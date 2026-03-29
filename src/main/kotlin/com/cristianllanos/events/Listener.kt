package com.cristianllanos.events

/**
 * A handler for events of type [T].
 *
 * Implement this interface to react to specific events emitted through the event bus.
 * Listeners are resolved from the dependency container on each emit, so they support constructor injection.
 *
 * ```kotlin
 * class SendWelcomeEmail(private val mailer: Mailer) : Listener<UserCreated> {
 *     override fun handle(event: UserCreated) {
 *         mailer.send("Welcome, ${event.name}!")
 *     }
 * }
 * ```
 *
 * @param T the event type this listener handles
 */
interface Listener<T : Event> {
    /**
     * Called when an event of type [T] is emitted.
     *
     * @param event the emitted event instance
     */
    fun handle(event: T)
}
