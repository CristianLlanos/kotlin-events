package com.cristianllanos.events

import com.cristianllanos.container.Container
import com.cristianllanos.container.resolve
import com.cristianllanos.container.singleton

class EventServiceProvider {
    fun register(container: Container) {
        container.singleton<EventBus> { EventBus(this) }
        container.singleton<Emitter> { resolve<EventBus>() }
        container.singleton<Subscriber> { resolve<EventBus>() }
    }
}
