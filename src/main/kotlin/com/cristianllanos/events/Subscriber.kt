package com.cristianllanos.events

import kotlin.reflect.KClass

interface Subscriber {
    fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): Subscriber
    fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): Subscriber
    fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): Subscriber
    fun clear()
}

inline fun <reified E : Event, reified L : Listener<E>> Subscriber.subscribe(): Subscriber =
    subscribe(E::class.java, L::class.java)

inline fun <reified E : Event> Subscriber.subscribe(vararg listeners: KClass<out Listener<E>>): Subscriber =
    subscribe(E::class.java, *listeners)

inline fun <reified E : Event, reified L : Listener<E>> Subscriber.unsubscribe(): Subscriber =
    unsubscribe(E::class.java, L::class.java)
