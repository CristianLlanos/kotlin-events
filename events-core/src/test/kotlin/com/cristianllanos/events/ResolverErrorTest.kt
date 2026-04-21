package com.cristianllanos.events

import com.cristianllanos.container.Resolver
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ResolverErrorTest {

    @Test
    fun `resolver throwing is collected as an error`() {
        val resolver = object : Resolver {
            override fun <T : Any> resolve(type: Class<T>): T {
                throw IllegalStateException("no binding")
            }
        }
        val errors = mutableListOf<Throwable>()
        val bus = EventBus(resolver, onError = { errors.add(it) })

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, errors.size)
        assertIs<IllegalStateException>(errors[0])
    }

    @Test
    fun `resolver throwing does not prevent lambda listeners from firing`() {
        val resolver = object : Resolver {
            override fun <T : Any> resolve(type: Class<T>): T {
                throw IllegalStateException("no binding")
            }
        }
        val errors = mutableListOf<Throwable>()
        val received = mutableListOf<String>()
        val bus = EventBus(resolver, onError = { errors.add(it) })

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.on<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(1, errors.size)
        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `resolver returning wrong type produces IllegalArgumentException`() {
        val resolver = object : Resolver {
            @Suppress("UNCHECKED_CAST")
            override fun <T : Any> resolve(type: Class<T>): T = "not a listener" as T
        }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()

        val error = assertFailsWith<IllegalArgumentException> {
            bus.emit(UserCreated("Alice"))
        }
        assertEquals(true, error.message?.contains("expected Listener"))
    }

    @Test
    fun `resolver error with default onError rethrows`() {
        val resolver = object : Resolver {
            override fun <T : Any> resolve(type: Class<T>): T {
                throw RuntimeException("resolve failed")
            }
        }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()

        val error = assertFailsWith<RuntimeException> {
            bus.emit(UserCreated("Alice"))
        }
        assertEquals("resolve failed", error.message)
    }
}
