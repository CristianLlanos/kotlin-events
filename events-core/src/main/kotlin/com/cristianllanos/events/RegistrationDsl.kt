package com.cristianllanos.events

import kotlin.reflect.KClass

/**
 * DSL for bulk event-listener registration.
 *
 * ```kotlin
 * bus.register {
 *     UserCreated::class mappedTo listOf(SendWelcomeEmail::class, LogNewUser::class)
 *     OrderPlaced::class mappedTo listOf(NotifyWarehouse::class)
 * }
 * ```
 */
class RegistrationDsl internal constructor(private val subscriber: Subscriber) {
    /**
     * Maps an event type to a list of listener classes.
     */
    @Suppress("UNCHECKED_CAST")
    infix fun <E : Event> KClass<E>.mappedTo(listeners: List<KClass<out Listener<E>>>) {
        subscriber.subscribe(this.java, *listeners.toTypedArray())
    }
}
