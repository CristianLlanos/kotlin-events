package com.cristianllanos.events

import com.cristianllanos.container.Container
import com.cristianllanos.container.singleton
import com.cristianllanos.container.resolve
import org.junit.Test
import kotlin.test.assertEquals

class EventBusTest {

    @Test
    fun `full flow with real container auto-resolution`() {
        val container = Container()
        val bus = EventBus(container)

        val listener = UserCreatedListener()
        container.singleton<UserCreatedListener> { listener }

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, listener.received.size)
        assertEquals("Alice", listener.received[0].name)
    }

    @Test
    fun `subscribe returns subscriber for chaining`() {
        val resolver = FakeResolver()
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
            .subscribe<OrderPlaced, OrderPlacedListener>()
    }
}
