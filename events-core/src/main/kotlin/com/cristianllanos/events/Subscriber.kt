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

    /**
     * Registers a lambda [handler] for events of type [event].
     *
     * Lambda listeners bypass the resolver and are called directly.
     *
     * @return a [Subscription] that can be used to cancel the registration
     */
    fun <E : Event> on(event: Class<E>, handler: (E) -> Unit): Subscription

    /**
     * Registers a one-shot [listener] class for events of type [event].
     * The listener is automatically unsubscribed after the first invocation.
     *
     * @return this subscriber for chaining
     */
    fun <E : Event, L : Listener<E>> once(event: Class<E>, listener: Class<L>): Subscriber

    /**
     * Registers a one-shot lambda [handler] for events of type [event].
     * The handler is automatically cancelled after the first invocation.
     *
     * @return a [Subscription] that can be used to cancel the registration before it fires
     */
    fun <E : Event> once(event: Class<E>, handler: (E) -> Unit): Subscription

    /**
     * Registers a global lambda [handler] that receives ALL emitted events.
     *
     * @return a [Subscription] that can be used to cancel the registration
     */
    fun onAny(handler: (Event) -> Unit): Subscription

    /**
     * Adds a [middleware] to the dispatch pipeline.
     *
     * @return this subscriber for chaining
     */
    fun use(middleware: Middleware): Subscriber

    /**
     * Registers event-listener mappings using a DSL block.
     *
     * ```kotlin
     * bus.register {
     *     UserCreated::class mappedTo listOf(SendWelcomeEmail::class, LogNewUser::class)
     *     OrderPlaced::class mappedTo listOf(NotifyWarehouse::class)
     * }
     * ```
     *
     * @return this subscriber for chaining
     */
    fun register(block: RegistrationDsl.() -> Unit): Subscriber
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

/**
 * Registers a lambda [handler] for event [E] using reified type parameters.
 *
 * ```kotlin
 * val subscription = subscriber.on<UserCreated> { event -> println(event.name) }
 * subscription.cancel()
 * ```
 */
inline fun <reified E : Event> Subscriber.on(noinline handler: (E) -> Unit): Subscription =
    on(E::class.java, handler)

/**
 * Registers a one-shot listener [L] for event [E] using reified type parameters.
 */
inline fun <reified E : Event, reified L : Listener<E>> Subscriber.once(): Subscriber =
    once(E::class.java, L::class.java)

/**
 * Registers a one-shot lambda [handler] for event [E] using reified type parameters.
 */
inline fun <reified E : Event> Subscriber.once(noinline handler: (E) -> Unit): Subscription =
    once(E::class.java, handler)
