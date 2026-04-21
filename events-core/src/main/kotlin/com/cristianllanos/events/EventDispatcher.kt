package com.cristianllanos.events

import com.cristianllanos.container.Resolver
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

internal class EventDispatcher(
    private val resolver: Resolver,
    private val onError: (Throwable) -> Unit = { throw it },
) : EventBus {

    private val lock = Any()
    private val classListeners = mutableMapOf<Class<out Event>, LinkedHashSet<Class<*>>>()
    private val lambdaListeners = mutableMapOf<Class<out Event>, MutableList<LambdaEntry<*>>>()
    private val globalListeners = mutableListOf<(Event) -> Unit>()
    private val middlewares = mutableListOf<Middleware>()
    private var cachedChain: ((Event) -> Unit)? = null

    // -- Subscriber: class-based registration --

    override fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): Subscriber {
        synchronized(lock) {
            classListeners.computeIfAbsent(event) { linkedSetOf() }.add(listener)
        }
        return this
    }

    override fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): Subscriber {
        synchronized(lock) {
            val set = classListeners.computeIfAbsent(event) { linkedSetOf() }
            listeners.forEach { set.add(it.java) }
        }
        return this
    }

    override fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): Subscriber {
        synchronized(lock) { classListeners[event]?.remove(listener) }
        return this
    }

    // -- Subscriber: lambda registration --

    override fun <E : Event> on(event: Class<E>, handler: (E) -> Unit): Subscription {
        val entry = LambdaEntry(handler)
        synchronized(lock) {
            lambdaListeners.computeIfAbsent(event) { mutableListOf() }.add(entry)
        }
        return Subscription {
            synchronized(lock) { lambdaListeners[event]?.remove(entry) }
        }
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
        val fired = AtomicBoolean(false)
        lateinit var entry: LambdaEntry<E>
        entry = LambdaEntry { e ->
            if (fired.compareAndSet(false, true)) {
                synchronized(lock) { lambdaListeners[event]?.remove(entry) }
                handler(e)
            }
        }
        synchronized(lock) {
            lambdaListeners.computeIfAbsent(event) { mutableListOf() }.add(entry)
        }
        return Subscription {
            if (fired.compareAndSet(false, true)) {
                synchronized(lock) { lambdaListeners[event]?.remove(entry) }
            }
        }
    }

    override fun onAny(handler: (Event) -> Unit): Subscription {
        synchronized(lock) { globalListeners.add(handler) }
        return Subscription {
            synchronized(lock) { globalListeners.remove(handler) }
        }
    }

    // -- Subscriber: middleware & DSL --

    override fun use(middleware: Middleware): Subscriber {
        synchronized(lock) {
            middlewares.add(middleware)
            cachedChain = null
        }
        return this
    }

    override fun register(block: RegistrationDsl.() -> Unit): Subscriber {
        RegistrationDsl(this).block()
        return this
    }

    override fun clear() {
        synchronized(lock) {
            classListeners.clear()
            lambdaListeners.clear()
            globalListeners.clear()
            middlewares.clear()
            cachedChain = null
        }
    }

    // -- Emitter --

    override fun <T : Event> emit(event: T) {
        val chain = synchronized(lock) {
            cachedChain ?: buildChain(middlewares) { e -> dispatchDirect(e) }.also { cachedChain = it }
        }
        chain(event)
    }

    override fun emit(first: Event, vararg rest: Event) {
        emit(first)
        rest.forEach { emit(it) }
    }

    // -- Inspector --

    override fun <E : Event> hasListeners(event: Class<E>): Boolean = synchronized(lock) {
        if (globalListeners.isNotEmpty()) return true
        if (collectEntries(classListeners, event).isNotEmpty()) return true
        if (collectEntries(lambdaListeners, event).isNotEmpty()) return true
        return false
    }

    override fun <E : Event> listenerCount(event: Class<E>): Int = synchronized(lock) {
        val classCount = collectEntries(classListeners, event).size
        val lambdaCount = collectEntries(lambdaListeners, event).size
        classCount + lambdaCount + globalListeners.size
    }

    // -- Internal dispatch --

    @Suppress("UNCHECKED_CAST")
    private fun dispatchDirect(event: Event) {
        val classSnapshot: List<Class<*>>
        val lambdaSnapshot: List<LambdaEntry<*>>
        val globalSnapshot: List<(Event) -> Unit>

        synchronized(lock) {
            classSnapshot = collectEntries(classListeners, event.javaClass)
            lambdaSnapshot = collectEntries(lambdaListeners, event.javaClass)
            globalSnapshot = if (globalListeners.isEmpty()) emptyList() else globalListeners.toList()
        }

        var errors: MutableList<Throwable>? = null

        for (listenerClass in classSnapshot) {
            try {
                val raw = resolver.resolve(listenerClass)
                require(raw is Listener<*>) {
                    "Resolver returned ${raw::class.java.name} for ${listenerClass.name}, expected Listener"
                }
                (raw as Listener<Event>).handle(event)
            } catch (e: Throwable) {
                (errors ?: mutableListOf<Throwable>().also { errors = it }).add(e)
            }
        }

        for (entry in lambdaSnapshot) {
            try {
                (entry as LambdaEntry<Event>).handler(event)
            } catch (e: Throwable) {
                (errors ?: mutableListOf<Throwable>().also { errors = it }).add(e)
            }
        }

        for (handler in globalSnapshot) {
            try {
                handler(event)
            } catch (e: Throwable) {
                (errors ?: mutableListOf<Throwable>().also { errors = it }).add(e)
            }
        }

        errors?.let { onError(it.toSingleOrComposite()) }
    }

    private class LambdaEntry<E>(val handler: (E) -> Unit)
}
