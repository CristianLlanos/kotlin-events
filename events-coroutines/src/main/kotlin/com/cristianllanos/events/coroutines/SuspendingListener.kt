package com.cristianllanos.events.coroutines

import com.cristianllanos.events.Event

/**
 * A suspending handler for events of type [T].
 *
 * Implement this interface when the handler needs to perform suspend operations
 * such as database writes, network calls, or channel sends.
 *
 * ```kotlin
 * class SendWelcomeEmail(private val mailer: SuspendingMailer) : SuspendingListener<UserCreated> {
 *     override suspend fun handle(event: UserCreated) {
 *         mailer.send("Welcome, ${event.name}!")
 *     }
 * }
 * ```
 *
 * @param T the event type this listener handles
 */
interface SuspendingListener<T : Event> {
    /** Called when an event of type [T] is emitted. */
    suspend fun handle(event: T)
}
