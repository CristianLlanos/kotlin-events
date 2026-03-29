package com.cristianllanos.events

import com.cristianllanos.container.Resolver

/**
 * Combined interface for emitting events and managing subscriptions.
 *
 * Prefer injecting [Emitter] or [Subscriber] individually for better separation of concerns.
 * Use `EventBus` when a component needs both capabilities.
 */
interface EventBus : Emitter, Subscriber

/**
 * Creates an [EventBus] instance that resolves listeners from the given [resolver].
 *
 * Listeners are instantiated on each [Emitter.emit] call, enabling constructor injection.
 *
 * ```kotlin
 * val bus = EventBus(container)
 * bus.subscribe<UserCreated, SendWelcomeEmail>()
 * bus.emit(UserCreated("Alice"))
 * ```
 */
fun EventBus(resolver: Resolver): EventBus = EventDispatcher(resolver)
