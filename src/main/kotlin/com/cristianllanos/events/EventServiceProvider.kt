package com.cristianllanos.events

import com.cristianllanos.container.Container
import com.cristianllanos.container.ServiceProvider
import com.cristianllanos.container.singleton

class EventServiceProvider : ServiceProvider {
    override fun register(container: Container) {
        container.singleton<EventBus> { EventBus(container) }
        container.singleton<Emitter> { container.resolve(EventBus::class.java) }
        container.singleton<Subscriber> { container.resolve(EventBus::class.java) }
    }
}
