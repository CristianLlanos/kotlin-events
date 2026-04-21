package com.cristianllanos.events.coroutines

import com.cristianllanos.container.Resolver
import com.cristianllanos.events.CompositeEventException
import com.cristianllanos.events.Event
import com.cristianllanos.events.Listener
import com.cristianllanos.events.Subscription
import kotlin.reflect.KClass

internal class SuspendingEventDispatcher(
    private val resolver: Resolver,
    private val onError: (Throwable) -> Unit = { throw it },
) : SuspendingEventBus {

    private val classListeners = mutableMapOf<Class<out Event>, LinkedHashSet<Class<*>>>()
    private val suspendingClassListeners = mutableMapOf<Class<out Event>, LinkedHashSet<Class<*>>>()
    private val lambdaListeners = mutableMapOf<Class<out Event>, MutableList<LambdaEntry<*>>>()
    private val globalListeners = mutableListOf<suspend (Event) -> Unit>()

    // -- SuspendingEmitter --

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Event> emit(event: T) {
        val errors = mutableListOf<Throwable>()

        // 1. Plain Listener class registrations (with hierarchy walk)
        for (listenerClass in collectEntries(classListeners, event::class.java)) {
            try {
                val listener = resolver.resolve(listenerClass) as Listener<T>
                listener.handle(event)
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        // 2. SuspendingListener class registrations (with hierarchy walk)
        for (listenerClass in collectEntries(suspendingClassListeners, event::class.java)) {
            try {
                val listener = resolver.resolve(listenerClass) as SuspendingListener<T>
                listener.handle(event)
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        // 3. Lambda registrations (with hierarchy walk)
        for (entry in collectEntries(lambdaListeners, event::class.java)) {
            try {
                (entry as LambdaEntry<Event>).handler(event)
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        // 4. Global listeners
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

    override suspend fun emit(first: Event, vararg rest: Event) {
        emit(first)
        rest.forEach { emit(it) }
    }

    // -- Plain Listener subscription --

    override fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        classListeners.computeIfAbsent(event) { linkedSetOf() }.add(listener)
        return this
    }

    override fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): SuspendingSubscriber {
        val set = classListeners.computeIfAbsent(event) { linkedSetOf() }
        listeners.forEach { set.add(it.java) }
        return this
    }

    override fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        classListeners[event]?.remove(listener)
        return this
    }

    // -- SuspendingListener subscription --

    override fun <E : Event, L : SuspendingListener<E>> subscribeSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        suspendingClassListeners.computeIfAbsent(event) { linkedSetOf() }.add(listener)
        return this
    }

    override fun <E : Event> subscribeSuspending(event: Class<E>, vararg listeners: KClass<out SuspendingListener<E>>): SuspendingSubscriber {
        val set = suspendingClassListeners.computeIfAbsent(event) { linkedSetOf() }
        listeners.forEach { set.add(it.java) }
        return this
    }

    override fun <E : Event, L : SuspendingListener<E>> unsubscribeSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        suspendingClassListeners[event]?.remove(listener)
        return this
    }

    // -- Lambda subscription --

    @Suppress("UNCHECKED_CAST")
    override fun <E : Event> on(event: Class<E>, handler: suspend (E) -> Unit): Subscription {
        val entry = LambdaEntry(handler as suspend (Any) -> Unit)
        lambdaListeners.computeIfAbsent(event) { mutableListOf() }.add(entry)
        return Subscription { lambdaListeners[event]?.remove(entry) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Event> once(event: Class<E>, handler: suspend (E) -> Unit): Subscription {
        lateinit var entry: LambdaEntry<Any>
        entry = LambdaEntry { e ->
            lambdaListeners[event]?.remove(entry)
            (handler as suspend (Any) -> Unit)(e)
        }
        lambdaListeners.computeIfAbsent(event) { mutableListOf() }.add(entry)
        return Subscription { lambdaListeners[event]?.remove(entry) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Event, L : Listener<E>> once(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        once(event) { e: E ->
            val resolved = resolver.resolve(listener) as Listener<E>
            resolved.handle(e)
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <E : Event, L : SuspendingListener<E>> onceSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        once(event) { e: E ->
            val resolved = resolver.resolve(listener) as SuspendingListener<E>
            resolved.handle(e)
        }
        return this
    }

    override fun onAny(handler: suspend (Event) -> Unit): Subscription {
        globalListeners.add(handler)
        return Subscription { globalListeners.remove(handler) }
    }

    override fun clear() {
        classListeners.clear()
        suspendingClassListeners.clear()
        lambdaListeners.clear()
        globalListeners.clear()
    }

    // -- Inspector (walks hierarchy for consistency with emit) --

    override fun <E : Event> hasListeners(event: Class<E>): Boolean {
        if (globalListeners.isNotEmpty()) return true
        if (collectEntries(classListeners, event).isNotEmpty()) return true
        if (collectEntries(suspendingClassListeners, event).isNotEmpty()) return true
        if (collectEntries(lambdaListeners, event).isNotEmpty()) return true
        return false
    }

    override fun <E : Event> listenerCount(event: Class<E>): Int {
        val classCount = collectEntries(classListeners, event).size
        val suspendingCount = collectEntries(suspendingClassListeners, event).size
        val lambdaCount = collectEntries(lambdaListeners, event).size
        return classCount + suspendingCount + lambdaCount + globalListeners.size
    }

    // -- Hierarchy walk (generic) --

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

    private class LambdaEntry<E>(val handler: suspend (E) -> Unit)
}
