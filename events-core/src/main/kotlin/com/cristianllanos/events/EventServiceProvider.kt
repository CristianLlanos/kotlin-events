package com.cristianllanos.events

import com.cristianllanos.container.Container
import com.cristianllanos.container.resolve
import com.cristianllanos.container.singleton

/**
 * Registers the event bus with a [Container] from kotlin-container.
 *
 * Registers singletons for [EventBus], [Emitter], and [Subscriber] so they can be
 * injected into any component resolved from the container.
 *
 * ```kotlin
 * val container = Container()
 * EventServiceProvider().register(container)
 *
 * val emitter = container.resolve<Emitter>()
 * emitter.emit(UserCreated("Alice"))
 * ```
 */
class EventServiceProvider {
    /** Registers [EventBus], [Emitter], and [Subscriber] singletons in the [container]. */
    fun register(container: Container) {
        container.singleton<EventBus> { EventBus(this) }
        container.singleton<Emitter> { resolve<EventBus>() }
        container.singleton<Subscriber> { resolve<EventBus>() }
    }
}
