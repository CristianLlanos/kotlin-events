package com.cristianllanos.events.coroutines

import com.cristianllanos.events.Event

/**
 * Publishes events to all registered listeners, suspending until all handlers complete.
 *
 * Both [SuspendingListener] and plain [com.cristianllanos.events.Listener] registrations
 * are invoked. Use this interface when a component only needs to fire events.
 *
 * ```kotlin
 * class UserService(private val emitter: SuspendingEmitter) {
 *     suspend fun createUser(name: String) {
 *         // ... create user
 *         emitter.emit(UserCreated(name))
 *     }
 * }
 * ```
 */
interface SuspendingEmitter {
    /**
     * Emits [event] and suspends until all registered listeners have finished handling it.
     */
    suspend fun <T : Event> emit(event: T)

    /**
     * Emits multiple events in order. Each event is fully dispatched before the next.
     */
    suspend fun emit(first: Event, vararg rest: Event)
}
