package com.cristianllanos.events

import com.cristianllanos.container.Resolver
import kotlin.reflect.KClass

internal class EventDispatcher(
    private val resolver: Resolver,
    private val onError: (Throwable) -> Unit = { throw it },
) : EventBus {

    private val classListeners = mutableMapOf<Class<out Event>, LinkedHashSet<Class<*>>>()
    private val lambdaListeners = mutableMapOf<Class<out Event>, MutableList<LambdaEntry<*>>>()
    private val globalListeners = mutableListOf<(Event) -> Unit>()
    private val middlewares = mutableListOf<Middleware>()

    // -- Subscriber: class-based registration --

    override fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): Subscriber {
        classListeners.computeIfAbsent(event) { linkedSetOf() }.add(listener)
        return this
    }

    override fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): Subscriber {
        val set = classListeners.computeIfAbsent(event) { linkedSetOf() }
        listeners.forEach { set.add(it.java) }
        return this
    }

    override fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): Subscriber {
        classListeners[event]?.remove(listener)
        return this
    }

    // -- Subscriber: lambda registration --

    override fun <E : Event> on(event: Class<E>, handler: (E) -> Unit): Subscription {
        val entry = LambdaEntry(handler)
        lambdaListeners.computeIfAbsent(event) { mutableListOf() }.add(entry)
        return Subscription { lambdaListeners[event]?.remove(entry) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Event, L : Listener<E>> once(event: Class<E>, listener: Class<L>): Subscriber {
        once(event) { e: E ->
            val resolved = resolver.resolve(listener) as Listener<E>
            resolved.handle(e)
        }
        return this
    }

    override fun <E : Event> once(event: Class<E>, handler: (E) -> Unit): Subscription {
        lateinit var entry: LambdaEntry<E>
        entry = LambdaEntry { e ->
            lambdaListeners[event]?.remove(entry)
            handler(e)
        }
        lambdaListeners.computeIfAbsent(event) { mutableListOf() }.add(entry)
        return Subscription { lambdaListeners[event]?.remove(entry) }
    }

    override fun onAny(handler: (Event) -> Unit): Subscription {
        globalListeners.add(handler)
        return Subscription { globalListeners.remove(handler) }
    }

    // -- Subscriber: middleware & DSL --

    override fun use(middleware: Middleware): Subscriber {
        middlewares.add(middleware)
        return this
    }

    override fun register(block: RegistrationDsl.() -> Unit): Subscriber {
        RegistrationDsl(this).block()
        return this
    }

    override fun clear() {
        classListeners.clear()
        lambdaListeners.clear()
        globalListeners.clear()
    }

    // -- Emitter --

    override fun <T : Event> emit(event: T) {
        val chain = buildChain(middlewares) { e -> dispatchDirect(e) }
        chain(event)
    }

    override fun emit(first: Event, vararg rest: Event) {
        emit(first)
        rest.forEach { emit(it) }
    }

    // -- Inspector (walks hierarchy for consistency with emit) --

    override fun <E : Event> hasListeners(event: Class<E>): Boolean {
        if (globalListeners.isNotEmpty()) return true
        val classEntries = collectEntries(classListeners, event)
        if (classEntries.isNotEmpty()) return true
        val lambdaEntries = collectEntries(lambdaListeners, event)
        return lambdaEntries.isNotEmpty()
    }

    override fun <E : Event> listenerCount(event: Class<E>): Int {
        val classCount = collectEntries(classListeners, event).size
        val lambdaCount = collectEntries(lambdaListeners, event).size
        return classCount + lambdaCount + globalListeners.size
    }

    // -- Internal dispatch --

    @Suppress("UNCHECKED_CAST")
    private fun dispatchDirect(event: Event) {
        val errors = mutableListOf<Throwable>()

        // Dispatch class-based listeners (with hierarchy walk)
        for (listenerClass in collectEntries(classListeners, event::class.java)) {
            try {
                val listener = resolver.resolve(listenerClass) as Listener<Event>
                listener.handle(event)
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        // Dispatch lambda listeners (with hierarchy walk)
        for (entry in collectEntries(lambdaListeners, event::class.java)) {
            try {
                (entry as LambdaEntry<Event>).handler(event)
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        // Dispatch global listeners
        for (handler in globalListeners.toList()) {
            try {
                handler(event)
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        if (errors.isNotEmpty()) {
            val error = if (errors.size == 1) errors[0] else CompositeEventException(errors)
            onError(error)
        }
    }

    // -- Hierarchy walk (generic, works for both registries) --

    private fun <V> collectEntries(registry: Map<out Class<out Event>, Collection<V>>, type: Class<*>): List<V> {
        val result = mutableListOf<V>()
        val visited = mutableSetOf<Class<*>>()
        collectRecursive(registry, type, result, visited)
        return result
    }

    private fun <V> collectRecursive(
        registry: Map<out Class<out Event>, Collection<V>>,
        type: Class<*>,
        result: MutableList<V>,
        visited: MutableSet<Class<*>>,
    ) {
        if (!visited.add(type)) return
        registry[type]?.let { result.addAll(it) }
        for (iface in type.interfaces) {
            collectRecursive(registry, iface, result, visited)
        }
        type.superclass?.let { superclass ->
            if (superclass != Any::class.java) {
                collectRecursive(registry, superclass, result, visited)
            }
        }
    }

    // -- Internal types --

    private class LambdaEntry<E>(val handler: (E) -> Unit)
}
