package com.cristianllanos.events

/**
 * Intercepts event dispatch for cross-cutting concerns like logging, metrics, or tracing.
 *
 * Middleware wraps the dispatch pipeline. Call [next] to continue to the next middleware
 * or the actual listener dispatch. Omitting the [next] call short-circuits the pipeline.
 *
 * ```kotlin
 * bus.use { event, next ->
 *     val start = System.nanoTime()
 *     next(event)
 *     println("Dispatched ${event::class.simpleName} in ${System.nanoTime() - start}ns")
 * }
 * ```
 */
fun interface Middleware {
    fun handle(event: Event, next: (Event) -> Unit)
}

internal fun buildChain(
    middlewares: List<Middleware>,
    core: (Event) -> Unit,
): (Event) -> Unit {
    return middlewares.foldRight(core) { middleware, next ->
        { event -> middleware.handle(event, next) }
    }
}
