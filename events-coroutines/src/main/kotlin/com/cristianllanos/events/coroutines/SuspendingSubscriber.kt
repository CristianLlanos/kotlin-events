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

    /** Registers a plain [listener] class for events of type [event]. */
    fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): SuspendingSubscriber

    /** Registers multiple plain [listeners] for events of type [event]. */
    fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): SuspendingSubscriber

    /** Removes a plain [listener] class from events of type [event]. */
    fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): SuspendingSubscriber

    /** Registers a suspending [listener] class for events of type [event]. */
    fun <E : Event, L : SuspendingListener<E>> subscribeSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber

    /** Registers multiple suspending [listeners] for events of type [event]. */
    fun <E : Event> subscribeSuspending(event: Class<E>, vararg listeners: KClass<out SuspendingListener<E>>): SuspendingSubscriber

    /** Removes a suspending [listener] class from events of type [event]. */
    fun <E : Event, L : SuspendingListener<E>> unsubscribeSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber

    /** Registers a suspending lambda [handler] for events of type [event]. */
    fun <E : Event> on(event: Class<E>, handler: suspend (E) -> Unit): Subscription

    /** Registers a one-shot suspending lambda [handler] that auto-unsubscribes after the first invocation. */
    fun <E : Event> once(event: Class<E>, handler: suspend (E) -> Unit): Subscription

    /** Registers a one-shot plain [listener] that auto-unsubscribes after the first invocation. */
    fun <E : Event, L : Listener<E>> once(event: Class<E>, listener: Class<L>): SuspendingSubscriber

    /** Registers a one-shot suspending [listener] that auto-unsubscribes after the first invocation. */
    fun <E : Event, L : SuspendingListener<E>> onceSuspending(event: Class<E>, listener: Class<L>): SuspendingSubscriber

    /** Registers a suspending [handler] that receives all emitted events. */
    fun onAny(handler: suspend (Event) -> Unit): Subscription

    /** Removes all registered listeners. */
    fun clear()
}

/** Registers plain listener [L] for event [E] using reified type parameters. */
inline fun <reified E : Event, reified L : Listener<E>> SuspendingSubscriber.subscribe(): SuspendingSubscriber =
    subscribe(E::class.java, L::class.java)

/** Registers multiple plain [listeners] for event [E] using reified type parameters. */
inline fun <reified E : Event> SuspendingSubscriber.subscribe(vararg listeners: KClass<out Listener<E>>): SuspendingSubscriber =
    subscribe(E::class.java, *listeners)

/** Removes plain listener [L] from event [E] using reified type parameters. */
inline fun <reified E : Event, reified L : Listener<E>> SuspendingSubscriber.unsubscribe(): SuspendingSubscriber =
    unsubscribe(E::class.java, L::class.java)

/** Registers suspending listener [L] for event [E] using reified type parameters. */
inline fun <reified E : Event, reified L : SuspendingListener<E>> SuspendingSubscriber.subscribeSuspending(): SuspendingSubscriber =
    subscribeSuspending(E::class.java, L::class.java)

/** Registers multiple suspending [listeners] for event [E] using reified type parameters. */
inline fun <reified E : Event> SuspendingSubscriber.subscribeSuspending(vararg listeners: KClass<out SuspendingListener<E>>): SuspendingSubscriber =
    subscribeSuspending(E::class.java, *listeners)

/** Removes suspending listener [L] from event [E] using reified type parameters. */
inline fun <reified E : Event, reified L : SuspendingListener<E>> SuspendingSubscriber.unsubscribeSuspending(): SuspendingSubscriber =
    unsubscribeSuspending(E::class.java, L::class.java)

/** Registers a suspending lambda [handler] for event [E] using reified type parameters. */
inline fun <reified E : Event> SuspendingSubscriber.on(noinline handler: suspend (E) -> Unit): Subscription =
    on(E::class.java, handler)

/** Registers a one-shot suspending lambda [handler] for event [E] using reified type parameters. */
inline fun <reified E : Event> SuspendingSubscriber.once(noinline handler: suspend (E) -> Unit): Subscription =
    once(E::class.java, handler)

/** Registers a one-shot plain listener [L] for event [E] using reified type parameters. */
inline fun <reified E : Event, reified L : Listener<E>> SuspendingSubscriber.once(): SuspendingSubscriber =
    once(E::class.java, L::class.java)

/** Registers a one-shot suspending listener [L] for event [E] using reified type parameters. */
inline fun <reified E : Event, reified L : SuspendingListener<E>> SuspendingSubscriber.onceSuspending(): SuspendingSubscriber =
    onceSuspending(E::class.java, L::class.java)
