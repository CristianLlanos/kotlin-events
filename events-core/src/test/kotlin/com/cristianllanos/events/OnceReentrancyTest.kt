package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class OnceReentrancyTest {

    @Test
    fun `once handler does not fire on reentrant emit`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<String>()

        bus.once<UserCreated> { e ->
            received.add(e.name)
            if (e.name == "Alice") {
                bus.emit(UserCreated("nested"))
            }
        }

        bus.emit(UserCreated("Alice"))

        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `once class-based does not fire on reentrant emit`() {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = EventBus(resolver)

        bus.once<UserCreated, UserCreatedListener>()
        bus.on<UserCreated> { e ->
            if (e.name == "first") bus.emit(UserCreated("reentrant"))
        }

        bus.emit(UserCreated("first"))

        assertEquals(1, listener.received.size)
        assertEquals("first", listener.received[0].name)
    }

    @Test
    fun `cancelled once does not fire`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<String>()

        val subscription = bus.once<UserCreated> { received.add(it.name) }
        subscription.cancel()
        bus.emit(UserCreated("Alice"))

        assertEquals(emptyList(), received)
    }

    @Test
    fun `cancel after fire is a no-op`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<String>()

        val subscription = bus.once<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))
        subscription.cancel()
        bus.emit(UserCreated("Bob"))

        assertEquals(listOf("Alice"), received)
    }
}
