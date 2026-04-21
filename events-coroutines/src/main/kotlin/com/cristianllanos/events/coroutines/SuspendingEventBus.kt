package com.cristianllanos.events.coroutines

import com.cristianllanos.container.Resolver
import com.cristianllanos.events.Inspector

/**
 * Combined interface for suspending event emission, subscription management, and inspection.
 *
 * Prefer injecting [SuspendingEmitter] or [SuspendingSubscriber] individually for
 * better separation of concerns. Use `SuspendingEventBus` when multiple capabilities are needed.
 */
interface SuspendingEventBus : SuspendingEmitter, SuspendingSubscriber, Inspector

/**
 * Creates a [SuspendingEventBus] that resolves class-registered listeners from [resolver].
 *
 * When a listener throws, remaining listeners still execute. Errors are collected and
 * passed to [onError] — by default, the exception is rethrown (single errors unwrapped,
 * multiple errors wrapped in [CompositeEventException][com.cristianllanos.events.CompositeEventException]).
 *
 * ```kotlin
 * val bus = SuspendingEventBus(container)
 * bus.subscribeSuspending<UserCreated, SendWelcomeEmail>()
 * bus.on<UserCreated> { event -> println(event.name) }
 *
 * coroutineScope { bus.emit(UserCreated("Alice")) }
 * ```
 */
fun SuspendingEventBus(
    resolver: Resolver,
    onError: (Throwable) -> Unit = { throw it },
): SuspendingEventBus = SuspendingEventDispatcher(resolver, onError)
