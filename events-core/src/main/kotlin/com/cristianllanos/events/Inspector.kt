package com.cristianllanos.events

/**
 * Provides introspection into the event bus state.
 *
 * Use this to query whether listeners are registered for a given event type,
 * useful for conditional logic and debugging.
 */
interface Inspector {
    /** Returns `true` if at least one listener is registered for [event] type. */
    fun <E : Event> hasListeners(event: Class<E>): Boolean

    /** Returns the number of listeners registered for [event] type. */
    fun <E : Event> listenerCount(event: Class<E>): Int
}

/** Returns `true` if at least one listener is registered for event type [E]. */
inline fun <reified E : Event> Inspector.hasListeners(): Boolean =
    hasListeners(E::class.java)

/** Returns the number of listeners registered for event type [E]. */
inline fun <reified E : Event> Inspector.listenerCount(): Int =
    listenerCount(E::class.java)
