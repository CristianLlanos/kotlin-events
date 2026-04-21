package com.cristianllanos.events.coroutines

import com.cristianllanos.events.Listener
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SuspendingOnceTest {

    @Test
    fun `once suspending lambda fires only once`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<String>()

        bus.once<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))
        bus.emit(UserCreated("Bob"))

        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `once plain listener fires only once`() = runTest {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = SuspendingEventBus(resolver)

        bus.once<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))
        bus.emit(UserCreated("Bob"))

        assertEquals(1, listener.received.size)
        assertEquals("Alice", listener.received[0].name)
    }

    @Test
    fun `onceSuspending class-based fires only once`() = runTest {
        val listener = SuspendingUserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = SuspendingEventBus(resolver)

        bus.onceSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.emit(UserCreated("Alice"))
        bus.emit(UserCreated("Bob"))

        assertEquals(1, listener.received.size)
        assertEquals("Alice", listener.received[0].name)
    }

    @Test
    fun `once subscription can be cancelled before firing`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<String>()

        val subscription = bus.once<UserCreated> { received.add(it.name) }
        subscription.cancel()
        bus.emit(UserCreated("Alice"))

        assertEquals(emptyList(), received)
    }

    @Test
    fun `cancel after fire is a no-op`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<String>()

        val subscription = bus.once<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))
        subscription.cancel()
        bus.emit(UserCreated("Bob"))

        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `unsubscribe plain listener`() = runTest {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = SuspendingEventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.unsubscribe<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(0, listener.received.size)
    }
}
