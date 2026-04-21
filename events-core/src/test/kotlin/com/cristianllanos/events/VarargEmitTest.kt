package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class VarargEmitTest {

    @Test
    fun `emit multiple events at once`() {
        val bus = EventBus(FakeResolver())
        val users = mutableListOf<String>()
        val orders = mutableListOf<Int>()

        bus.on<UserCreated> { users.add(it.name) }
        bus.on<OrderPlaced> { orders.add(it.orderId) }
        bus.emit(UserCreated("Alice"), OrderPlaced(42))

        assertEquals(listOf("Alice"), users)
        assertEquals(listOf(42), orders)
    }

    @Test
    fun `emit multiple events dispatches in order`() {
        val bus = EventBus(FakeResolver())
        val log = mutableListOf<String>()

        bus.on<UserCreated> { log.add("user:${it.name}") }
        bus.on<OrderPlaced> { log.add("order:${it.orderId}") }
        bus.emit(UserCreated("Alice"), OrderPlaced(1), UserCreated("Bob"), OrderPlaced(2))

        assertEquals(listOf("user:Alice", "order:1", "user:Bob", "order:2"), log)
    }
}
