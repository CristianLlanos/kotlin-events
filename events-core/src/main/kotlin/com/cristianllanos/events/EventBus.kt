package com.cristianllanos.events

import com.cristianllanos.container.Resolver

/**
 * Combined interface for emitting events, managing subscriptions, and inspecting state.
 *
 * Prefer injecting [Emitter] or [Subscriber] individually for better separation of concerns.
 * Use `EventBus` when a component needs multiple capabilities.
 */
interface EventBus : Emitter, Subscriber, Inspector

/**
 * Creates an [EventBus] instance that resolves listeners from the given [resolver].
 *
 * Listeners are instantiated on each [Emitter.emit] call, enabling constructor injection.
 * When a listener throws, remaining listeners still execute. Errors are collected and
 * passed to [onError] — by default, the exception is rethrown (single errors unwrapped,
 * multiple errors wrapped in [CompositeEventException]).
 *
 * ```kotlin
 * val bus = EventBus(container)
 * bus.subscribe<UserCreated, SendWelcomeEmail>()
 * bus.emit(UserCreated("Alice"))
 * ```
 */
fun EventBus(
    resolver: Resolver,
    onError: (Throwable) -> Unit = { throw it },
): EventBus = EventDispatcher(resolver, onError)
