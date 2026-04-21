package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class RegistrationDslTest {

    @Test
    fun `register DSL subscribes multiple listeners`() {
        val userListener = UserCreatedListener()
        val anotherListener = AnotherUserCreatedListener()
        val orderListener = OrderPlacedListener()
        val resolver = FakeResolver().apply {
            bind(userListener)
            bind(anotherListener)
            bind(orderListener)
        }
        val bus = EventBus(resolver)

        bus.register {
            UserCreated::class mappedTo listOf(UserCreatedListener::class, AnotherUserCreatedListener::class)
            OrderPlaced::class mappedTo listOf(OrderPlacedListener::class)
        }
        bus.emit(UserCreated("Alice"))
        bus.emit(OrderPlaced(42))

        assertEquals(1, userListener.received.size)
        assertEquals(1, anotherListener.received.size)
        assertEquals(1, orderListener.received.size)
    }
}
