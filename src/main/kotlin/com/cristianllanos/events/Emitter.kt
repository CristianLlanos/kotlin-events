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
     * @param event the event to emit
     */
    fun <T : Event> emit(event: T)
}
