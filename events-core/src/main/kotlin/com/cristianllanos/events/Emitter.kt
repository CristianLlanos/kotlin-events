package com.cristianllanos.events

/**
 * Publishes events to all registered listeners.
 *
 * Use this interface when a component only needs to fire events, not manage subscriptions.
 *
 * ```kotlin
 * class UserService(private val emitter: Emitter) {
 *     fun createUser(name: String) {
 *         // ... create user
 *         emitter.emit(UserCreated(name))
 *     }
 * }
 * ```
 */
interface Emitter {
    /**
     * Emits an [event], invoking all listeners registered for its type.
     *
     * Listeners registered for parent types in the event's class hierarchy are also invoked.
     *
     * @param event the event to emit
     */
    fun <T : Event> emit(event: T)

    /**
     * Emits multiple events in order.
     *
     * Each event is fully dispatched (all listeners called) before the next event is emitted.
     */
    fun emit(first: Event, vararg rest: Event)
}
