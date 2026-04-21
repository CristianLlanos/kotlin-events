package com.cristianllanos.events.coroutines

import com.cristianllanos.container.Resolver
import com.cristianllanos.events.Event
import com.cristianllanos.events.Listener
import com.cristianllanos.events.Subscription
import com.cristianllanos.events.collectEntries
import com.cristianllanos.events.toSingleOrComposite
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

internal class SuspendingEventDispatcher(
    private val resolver: Resolver,
    private val onError: suspend (Throwable) -> Unit = { throw it },
) : SuspendingEventBus {

    private val lock = Any()
    private val classListeners = mutableMapOf<Class<out Event>, LinkedHashSet<Class<*>>>()
    private val suspendingClassListeners = mutableMapOf<Class<out Event>, LinkedHashSet<Class<*>>>()
    private val lambdaListeners = mutableMapOf<Class<out Event>, MutableList<LambdaEntry<*>>>()
    private val globalListeners = mutableListOf<suspend (Event) -> Unit>()

    // -- SuspendingEmitter --

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Event> emit(event: T) {
        val classSnapshot: List<Class<*>>
        val suspendingSnapshot: List<Class<*>>
        val lambdaSnapshot: List<LambdaEntry<*>>
        val globalSnapshot: List<suspend (Event) -> Unit>

        synchronized(lock) {
            classSnapshot = collectEntries(classListeners, event.javaClass)
            suspendingSnapshot = collectEntries(suspendingClassListeners, event.javaClass)
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
                (raw as Listener<T>).handle(event)
            } catch (e: Throwable) {
                (errors ?: mutableListOf<Throwable>().also { errors = it }).add(e)
            }
        }

        for (listenerClass in suspendingSnapshot) {
            try {
                val raw = resolver.resolve(listenerClass)
                require(raw is SuspendingListener<*>) {
                    "Resolver returned ${raw::class.java.name} for ${listenerClass.name}, expected SuspendingListener"
                }
                (raw as SuspendingListener<T>).handle(event)
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

    override suspend fun emit(first: Event, vararg rest: Event) {
        emit(first)
        rest.forEach { emit(it) }
    }

    // -- Plain Listener subscription --

    override fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        synchronized(lock) {
            classListeners.computeIfAbsent(event) { linkedSetOf() }.add(listener)
        }
        return this
    }

    override fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): SuspendingSubscriber {
        synchronized(lock) {
            val set = classListeners.computeIfAbsent(event) { linkedSetOf() }
            listeners.forEach { set.add(it.java) }
        }
        return this
    }

    override fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        synchronized(lock) { classListeners[event]?.remove(listener) }
        return this
    }

    // -- SuspendingListener subscription --

    override fun <E : Event, L : SuspendingListener<E>> subscribeSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        synchronized(lock) {
            suspendingClassListeners.computeIfAbsent(event) { linkedSetOf() }.add(listener)
        }
        return this
    }

    override fun <E : Event> subscribeSuspending(event: Class<E>, vararg listeners: KClass<out SuspendingListener<E>>): SuspendingSubscriber {
        synchronized(lock) {
            val set = suspendingClassListeners.computeIfAbsent(event) { linkedSetOf() }
            listeners.forEach { set.add(it.java) }
        }
        return this
    }

    override fun <E : Event, L : SuspendingListener<E>> unsubscribeSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber {
        synchronized(lock) { suspendingClassListeners[event]?.remove(listener) }
        return this
    }

    // -- Lambda subscription --

    override fun <E : Event> on(event: Class<E>, handler: suspend (E) -> Unit): Subscription {
        val entry = LambdaEntry(handler)
        synchronized(lock) {
            lambdaListeners.computeIfAbsent(event) { mutableListOf() }.add(entry)
        }
        return Subscription {
            synchronized(lock) { lambdaListeners[event]?.remove(entry) }
        }
    }

    override fun <E : Event> once(event: Class<E>, handler: suspend (E) -> Unit): Subscription {
        val fired = AtomicBoolean(false)
        lateinit var entry: LambdaEntry<E>
        entry = LambdaEntry<E> { e ->
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
        synchronized(lock) { globalListeners.add(handler) }
        return Subscription {
            synchronized(lock) { globalListeners.remove(handler) }
        }
    }

    override fun clear() {
        synchronized(lock) {
            classListeners.clear()
            suspendingClassListeners.clear()
            lambdaListeners.clear()
            globalListeners.clear()
        }
    }

    // -- Inspector --

    override fun <E : Event> hasListeners(event: Class<E>): Boolean = synchronized(lock) {
        if (globalListeners.isNotEmpty()) return true
        if (collectEntries(classListeners, event).isNotEmpty()) return true
        if (collectEntries(suspendingClassListeners, event).isNotEmpty()) return true
        if (collectEntries(lambdaListeners, event).isNotEmpty()) return true
        return false
    }

    override fun <E : Event> listenerCount(event: Class<E>): Int = synchronized(lock) {
        val classCount = collectEntries(classListeners, event).size
        val suspendingCount = collectEntries(suspendingClassListeners, event).size
        val lambdaCount = collectEntries(lambdaListeners, event).size
        classCount + suspendingCount + lambdaCount + globalListeners.size
    }

    private class LambdaEntry<E>(val handler: suspend (E) -> Unit)
}
