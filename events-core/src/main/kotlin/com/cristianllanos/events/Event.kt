package com.cristianllanos.events

/**
 * Base type for all events in the event bus system.
 *
 * Implement this interface to define domain-specific events that can be emitted and handled by listeners.
 * Using an interface allows events to be data classes, enabling `copy()`, `equals()`, and `toString()` for free.
 *
 * ```kotlin
 * data class UserCreated(val name: String) : Event
 * data class OrderPlaced(val orderId: Int) : Event
 * ```
 */
interface Event
