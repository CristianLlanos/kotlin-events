package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ErrorResilienceTest {

    @Test
    fun `remaining listeners execute when one throws`() {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply {
            bind(FailingListener())
            bind(listener)
        }
        val errors = mutableListOf<Throwable>()
        val bus = EventBus(resolver, onError = { errors.add(it) })

        bus.subscribe<UserCreated, FailingListener>()
        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, listener.received.size)
        assertEquals(1, errors.size)
    }

    @Test
    fun `single error is thrown directly by default`() {
        val resolver = FakeResolver().apply { bind(FailingListener()) }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, FailingListener>()

        val error = assertFailsWith<RuntimeException> {
            bus.emit(UserCreated("Alice"))
        }
        assertEquals("Listener failed for Alice", error.message)
    }

    @Test
    fun `multiple errors are wrapped in CompositeEventException by default`() {
        val resolver = FakeResolver().apply {
            bind<FailingListener>(FailingListener())
            bind<AnotherFailingListener>(AnotherFailingListener())
        }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, FailingListener>()
        bus.subscribe<UserCreated, AnotherFailingListener>()

        val error = assertFailsWith<CompositeEventException> {
            bus.emit(UserCreated("Alice"))
        }
        assertEquals(2, error.errors.size)
    }

    @Test
    fun `custom onError handler receives errors`() {
        val resolver = FakeResolver().apply { bind(FailingListener()) }
        val collected = mutableListOf<Throwable>()
        val bus = EventBus(resolver, onError = { collected.add(it) })

        bus.subscribe<UserCreated, FailingListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, collected.size)
    }

    @Test
    fun `lambda listener errors are collected too`() {
        val resolver = FakeResolver()
        val errors = mutableListOf<Throwable>()
        val received = mutableListOf<String>()
        val bus = EventBus(resolver, onError = { errors.add(it) })

        bus.on<UserCreated> { throw RuntimeException("lambda fail") }
        bus.on<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(1, errors.size)
        assertEquals(1, received.size)
    }
}

private class AnotherFailingListener : Listener<UserCreated> {
    override fun handle(event: UserCreated) {
        throw RuntimeException("Another failure for ${event.name}")
    }
}
