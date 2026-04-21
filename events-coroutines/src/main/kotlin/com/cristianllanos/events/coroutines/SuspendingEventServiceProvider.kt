package com.cristianllanos.events.coroutines

import com.cristianllanos.container.Container
import com.cristianllanos.container.resolve
import com.cristianllanos.container.singleton

/**
 * Registers the suspending event bus with a [Container] from kotlin-container.
 *
 * Registers singletons for [SuspendingEventBus], [SuspendingEmitter], and
 * [SuspendingSubscriber] so they can be injected into any component resolved
 * from the container.
 *
 * ```kotlin
 * val container = Container()
 * SuspendingEventServiceProvider().register(container)
 *
 * val emitter = container.resolve<SuspendingEmitter>()
 * coroutineScope { emitter.emit(UserCreated("Alice")) }
 * ```
 */
class SuspendingEventServiceProvider {
    /** Registers [SuspendingEventBus], [SuspendingEmitter], and [SuspendingSubscriber] singletons in the [container]. */
    fun register(container: Container) {
        container.singleton<SuspendingEventBus> { SuspendingEventBus(this) }
        container.singleton<SuspendingEmitter> { resolve<SuspendingEventBus>() }
        container.singleton<SuspendingSubscriber> { resolve<SuspendingEventBus>() }
    }
}
