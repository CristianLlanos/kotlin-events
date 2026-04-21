package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class MiddlewareErrorTest {

    @Test
    fun `middleware runs even when listeners throw`() {
        val resolver = FakeResolver().apply { bind(FailingListener()) }
        val errors = mutableListOf<Throwable>()
        val log = mutableListOf<String>()
        val bus = EventBus(resolver, onError = { errors.add(it) })

        bus.use { event, next ->
            log.add("before")
            next(event)
            log.add("after")
        }
        bus.subscribe<UserCreated, FailingListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(listOf("before", "after"), log)
        assertEquals(1, errors.size)
    }

    @Test
    fun `middleware error is propagated`() {
        val bus = EventBus(FakeResolver())
        val errors = mutableListOf<Throwable>()
        val received = mutableListOf<String>()

        bus.use { _, _ -> throw RuntimeException("middleware failed") }
        bus.on<UserCreated> { received.add(it.name) }

        try {
            bus.emit(UserCreated("Alice"))
        } catch (e: RuntimeException) {
            errors.add(e)
        }

        assertEquals(1, errors.size)
        assertEquals("middleware failed", errors[0].message)
        assertEquals(emptyList(), received)
    }

    @Test
    fun `middleware and lambda errors are both collected`() {
        val errors = mutableListOf<Throwable>()
        val bus = EventBus(FakeResolver(), onError = { errors.add(it) })

        bus.use { event, next ->
            next(event)
        }
        bus.on<UserCreated> { throw RuntimeException("lambda fail") }
        bus.on<UserCreated> { throw RuntimeException("another fail") }
        bus.emit(UserCreated("Alice"))

        assertEquals(1, errors.size)
        val composite = errors[0] as CompositeEventException
        assertEquals(2, composite.errors.size)
    }
}
