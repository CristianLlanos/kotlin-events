package com.cristianllanos.events

import com.cristianllanos.container.Resolver
import kotlin.reflect.KClass

internal class EventDispatcher(
    private val resolver: Resolver,
) : EventBus {

    private val listeners = mutableMapOf<Class<out Event>, MutableList<Class<*>>>()

    override fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): Subscriber {
        listeners.computeIfAbsent(event) { mutableListOf() }.add(listener)
        return this
    }

    override fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): Subscriber {
        val list = this.listeners.computeIfAbsent(event) { mutableListOf() }
        listeners.forEach { list.add(it.java) }
        return this
    }

    override fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): Subscriber {
        listeners[event]?.remove(listener)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Event> emit(event: T) {
        listeners[event::class.java]?.forEach { listenerClass ->
            val listener = resolver.resolve(listenerClass) as Listener<T>
            listener.handle(event)
        }
    }

    override fun clear() {
        listeners.clear()
    }
}
