package com.cristianllanos.events

/**
 * Base class for all events in the event bus system.
 *
 * Extend this class to define domain-specific events that can be emitted and handled by listeners.
 *
 * ```kotlin
 * class UserCreated(val name: String) : Event()
 * class OrderPlaced(val orderId: Int) : Event()
 * ```
 */
open class Event
