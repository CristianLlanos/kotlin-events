package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class EmitTest {

    @Test
    fun `emits event to a registered listener`() {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, listener.received.size)
        assertEquals("Alice", listener.received[0].name)
    }

    @Test
    fun `emits event to multiple listeners`() {
        val first = UserCreatedListener()
        val second = AnotherUserCreatedListener()
        val resolver = FakeResolver().apply {
            bind(first)
            bind(second)
        }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.subscribe<UserCreated, AnotherUserCreatedListener>()
        bus.emit(UserCreated("Bob"))

        assertEquals(1, first.received.size)
        assertEquals(1, second.received.size)
    }

    @Test
    fun `emitting with no listeners does nothing`() {
        val resolver = FakeResolver()
        val bus = EventBus(resolver)

        bus.emit(UserCreated("Ghost"))
    }

    @Test
    fun `emitting one event type does not trigger other listeners`() {
        val userListener = UserCreatedListener()
        val orderListener = OrderPlacedListener()
        val resolver = FakeResolver().apply {
            bind(userListener)
            bind(orderListener)
        }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.subscribe<OrderPlaced, OrderPlacedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, userListener.received.size)
        assertEquals(0, orderListener.received.size)
    }
}
