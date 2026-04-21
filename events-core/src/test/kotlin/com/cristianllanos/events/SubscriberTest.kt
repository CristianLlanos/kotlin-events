package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class SubscriberTest {

    @Test
    fun `unsubscribe removes a listener`() {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.unsubscribe<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(0, listener.received.size)
    }

    @Test
    fun `subscribe multiple listeners at once`() {
        val first = UserCreatedListener()
        val second = AnotherUserCreatedListener()
        val resolver = FakeResolver().apply {
            bind(first)
            bind(second)
        }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated>(
            UserCreatedListener::class,
            AnotherUserCreatedListener::class,
        )
        bus.emit(UserCreated("Alice"))

        assertEquals(1, first.received.size)
        assertEquals(1, second.received.size)
    }

    @Test
    fun `unsubscribe non-existent listener does nothing`() {
        val resolver = FakeResolver()
        val bus = EventBus(resolver)

        bus.unsubscribe<UserCreated, UserCreatedListener>()
    }

    @Test
    fun `clear removes all listeners`() {
        val userListener = UserCreatedListener()
        val orderListener = OrderPlacedListener()
        val resolver = FakeResolver().apply {
            bind(userListener)
            bind(orderListener)
        }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.subscribe<OrderPlaced, OrderPlacedListener>()
        bus.clear()
        bus.emit(UserCreated("Alice"))
        bus.emit(OrderPlaced(1))

        assertEquals(0, userListener.received.size)
        assertEquals(0, orderListener.received.size)
    }
}
