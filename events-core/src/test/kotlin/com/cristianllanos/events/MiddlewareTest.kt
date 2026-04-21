package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class MiddlewareTest {

    @Test
    fun `middleware wraps dispatch`() {
        val bus = EventBus(FakeResolver())
        val log = mutableListOf<String>()

        bus.use { event, next ->
            log.add("before")
            next(event)
            log.add("after")
        }
        bus.on<UserCreated> { log.add("handler") }
        bus.emit(UserCreated("Alice"))

        assertEquals(listOf("before", "handler", "after"), log)
    }

    @Test
    fun `middleware can short-circuit`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<String>()

        bus.use { _, _ -> /* don't call next */ }
        bus.on<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(emptyList(), received)
    }

    @Test
    fun `multiple middlewares execute in order`() {
        val bus = EventBus(FakeResolver())
        val log = mutableListOf<String>()

        bus.use { event, next ->
            log.add("A-before")
            next(event)
            log.add("A-after")
        }
        bus.use { event, next ->
            log.add("B-before")
            next(event)
            log.add("B-after")
        }
        bus.on<UserCreated> { log.add("handler") }
        bus.emit(UserCreated("Alice"))

        assertEquals(listOf("A-before", "B-before", "handler", "B-after", "A-after"), log)
    }

    @Test
    fun `clear removes middleware`() {
        val bus = EventBus(FakeResolver())
        val log = mutableListOf<String>()

        bus.use { _, _ -> log.add("blocked") }
        bus.clear()
        bus.on<UserCreated> { log.add("handler") }
        bus.emit(UserCreated("Alice"))

        assertEquals(listOf("handler"), log)
    }

    @Test
    fun `middleware added after emit is picked up`() {
        val bus = EventBus(FakeResolver())
        val log = mutableListOf<String>()

        bus.on<UserCreated> { log.add("handler") }
        bus.emit(UserCreated("first"))

        bus.use { event, next ->
            log.add("middleware")
            next(event)
        }
        bus.emit(UserCreated("second"))

        assertEquals(listOf("handler", "middleware", "handler"), log)
    }
}
