package com.cristianllanos.events

/**
 * A handle to a lambda listener registration, used to cancel it later.
 *
 * Returned by [Subscriber.on], [Subscriber.once], and [Subscriber.onAny].
 *
 * ```kotlin
 * val subscription = bus.on<UserCreated> { println(it) }
 * subscription.cancel() // removes the listener
 * ```
 */
fun interface Subscription {
    /** Removes the associated listener from the event bus. */
    fun cancel()
}
