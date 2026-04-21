package com.cristianllanos.events.coroutines

import com.cristianllanos.events.Event
import com.cristianllanos.events.Listener
import com.cristianllanos.events.Subscription
import kotlin.reflect.KClass

/**
 * Manages listener registration for a suspending event bus.
 *
 * Accepts both [Listener] and [SuspendingListener] class registrations, as well as
 * anonymous suspending lambdas via [on]. Methods return `this` for fluent chaining.
 */
interface SuspendingSubscriber {

    // -- Plain Listener registration --

    fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): SuspendingSubscriber
    fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): SuspendingSubscriber
    fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): SuspendingSubscriber

    // -- SuspendingListener registration --

    fun <E : Event, L : SuspendingListener<E>> subscribeSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber
    fun <E : Event> subscribeSuspending(event: Class<E>, vararg listeners: KClass<out SuspendingListener<E>>): SuspendingSubscriber
    fun <E : Event, L : SuspendingListener<E>> unsubscribeSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber

    // -- Lambda registration --

    fun <E : Event> on(event: Class<E>, handler: suspend (E) -> Unit): Subscription
    fun <E : Event> once(event: Class<E>, handler: suspend (E) -> Unit): Subscription
    fun <E : Event, L : Listener<E>> once(event: Class<E>, listener: Class<L>): SuspendingSubscriber
    fun <E : Event, L : SuspendingListener<E>> onceSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber
    fun onAny(handler: suspend (Event) -> Unit): Subscription

    fun clear()
}

// -- Reified extensions for plain Listener --

inline fun <reified E : Event, reified L : Listener<E>> SuspendingSubscriber.subscribe(): SuspendingSubscriber =
    subscribe(E::class.java, L::class.java)

inline fun <reified E : Event> SuspendingSubscriber.subscribe(vararg listeners: KClass<out Listener<E>>): SuspendingSubscriber =
    subscribe(E::class.java, *listeners)

inline fun <reified E : Event, reified L : Listener<E>> SuspendingSubscriber.unsubscribe(): SuspendingSubscriber =
    unsubscribe(E::class.java, L::class.java)

// -- Reified extensions for SuspendingListener --

inline fun <reified E : Event, reified L : SuspendingListener<E>> SuspendingSubscriber.subscribeSuspending(): SuspendingSubscriber =
    subscribeSuspending(E::class.java, L::class.java)

inline fun <reified E : Event> SuspendingSubscriber.subscribeSuspending(vararg listeners: KClass<out SuspendingListener<E>>): SuspendingSubscriber =
    subscribeSuspending(E::class.java, *listeners)

inline fun <reified E : Event, reified L : SuspendingListener<E>> SuspendingSubscriber.unsubscribeSuspending(): SuspendingSubscriber =
    unsubscribeSuspending(E::class.java, L::class.java)

// -- Reified extensions for lambda --

inline fun <reified E : Event> SuspendingSubscriber.on(noinline handler: suspend (E) -> Unit): Subscription =
    on(E::class.java, handler)

inline fun <reified E : Event> SuspendingSubscriber.once(noinline handler: suspend (E) -> Unit): Subscription =
    once(E::class.java, handler)

inline fun <reified E : Event, reified L : Listener<E>> SuspendingSubscriber.once(): SuspendingSubscriber =
    once(E::class.java, L::class.java)

inline fun <reified E : Event, reified L : SuspendingListener<E>> SuspendingSubscriber.onceSuspending(): SuspendingSubscriber =
    onceSuspending(E::class.java, L::class.java)
