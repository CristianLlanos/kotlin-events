package com.cristianllanos.events

import kotlin.reflect.KClass

/**
 * Manages listener registration for events.
 *
 * Use this interface when a component only needs to register or remove listeners,
 * not emit events. Methods return `this` for fluent chaining.
 *
 * ```kotlin
 * subscriber
 *     .subscribe<UserCreated, SendWelcomeEmail>()
 *     .subscribe<UserCreated, LogNewUser>()
 *     .subscribe<OrderPlaced, NotifyWarehouse>()
 * ```
 */
interface Subscriber {
    /**
     * Registers a [listener] class for the given [event] type.
     *
     * @return this subscriber for chaining
     */
    fun <E : Event, L : Listener<E>> subscribe(event: Class<E>, listener: Class<L>): Subscriber

    /**
     * Registers multiple [listeners] for the given [event] type.
     *
     * @return this subscriber for chaining
     */
    fun <E : Event> subscribe(event: Class<E>, vararg listeners: KClass<out Listener<E>>): Subscriber

    /**
     * Removes a [listener] class from the given [event] type.
     *
     * @return this subscriber for chaining
     */
    fun <E : Event, L : Listener<E>> unsubscribe(event: Class<E>, listener: Class<L>): Subscriber

    /** Removes all registered listeners for all event types. */
    fun clear()
}

/**
 * Registers listener [L] for event [E] using reified type parameters.
 *
 * ```kotlin
 * subscriber.subscribe<UserCreated, SendWelcomeEmail>()
 * ```
 */
inline fun <reified E : Event, reified L : Listener<E>> Subscriber.subscribe(): Subscriber =
    subscribe(E::class.java, L::class.java)

/**
 * Registers multiple [listeners] for event [E] using reified type parameters.
 *
 * ```kotlin
 * subscriber.subscribe<UserCreated>(SendWelcomeEmail::class, LogNewUser::class)
 * ```
 */
inline fun <reified E : Event> Subscriber.subscribe(vararg listeners: KClass<out Listener<E>>): Subscriber =
    subscribe(E::class.java, *listeners)

/**
 * Removes listener [L] from event [E] using reified type parameters.
 */
inline fun <reified E : Event, reified L : Listener<E>> Subscriber.unsubscribe(): Subscriber =
    unsubscribe(E::class.java, L::class.java)
