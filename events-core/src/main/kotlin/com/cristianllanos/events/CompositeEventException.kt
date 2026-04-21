package com.cristianllanos.events

/**
 * Thrown when one or more listeners fail during event dispatch.
 *
 * Contains all [errors] collected during a single [Emitter.emit] call.
 * By default, if only one listener fails, its exception is thrown directly
 * without wrapping in [CompositeEventException].
 */
class CompositeEventException(
    val errors: List<Throwable>,
) : RuntimeException(
    "Event dispatch failed with ${errors.size} error(s): ${errors.map { it.message }}",
) {
    init {
        errors.forEach { addSuppressed(it) }
    }
}

fun List<Throwable>.toSingleOrComposite(): Throwable =
    if (size == 1) this[0] else CompositeEventException(this)
