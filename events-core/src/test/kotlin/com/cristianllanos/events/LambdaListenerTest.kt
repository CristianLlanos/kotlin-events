package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class LambdaListenerTest {

    @Test
    fun `on registers a lambda listener`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<String>()

        bus.on<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `subscription cancel removes the lambda`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<String>()

        val subscription = bus.on<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))
        subscription.cancel()
        bus.emit(UserCreated("Bob"))

        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `multiple lambda listeners for same event`() {
        val bus = EventBus(FakeResolver())
        val first = mutableListOf<String>()
        val second = mutableListOf<String>()

        bus.on<UserCreated> { first.add(it.name) }
        bus.on<UserCreated> { second.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(1, first.size)
        assertEquals(1, second.size)
    }

    @Test
    fun `once lambda fires only once`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<String>()

        bus.once<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))
        bus.emit(UserCreated("Bob"))

        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `once subscription can be cancelled before firing`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<String>()

        val subscription = bus.once<UserCreated> { received.add(it.name) }
        subscription.cancel()
        bus.emit(UserCreated("Alice"))

        assertEquals(emptyList(), received)
    }

    @Test
    fun `onAny receives all event types`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<Event>()

        bus.onAny { received.add(it) }
        bus.emit(UserCreated("Alice"))
        bus.emit(OrderPlaced(42))

        assertEquals(2, received.size)
        assertEquals("Alice", (received[0] as UserCreated).name)
        assertEquals(42, (received[1] as OrderPlaced).orderId)
    }

    @Test
    fun `onAny subscription can be cancelled`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<Event>()

        val subscription = bus.onAny { received.add(it) }
        bus.emit(UserCreated("Alice"))
        subscription.cancel()
        bus.emit(UserCreated("Bob"))

        assertEquals(1, received.size)
    }

    @Test
    fun `lambda and class listeners coexist`() {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = EventBus(resolver)
        val lambdaReceived = mutableListOf<String>()

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.on<UserCreated> { lambdaReceived.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(1, listener.received.size)
        assertEquals(1, lambdaReceived.size)
    }

    @Test
    fun `once class-based listener fires only once`() {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = EventBus(resolver)

        bus.once<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))
        bus.emit(UserCreated("Bob"))

        assertEquals(1, listener.received.size)
        assertEquals("Alice", listener.received[0].name)
    }

    @Test
    fun `once class-based does not interfere with subscribe`() {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.once<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))
        bus.emit(UserCreated("Bob"))

        // subscribe fires both times, once fires only first time = 3 total
        assertEquals(3, listener.received.size)
    }

    @Test
    fun `duplicate subscribe is deduplicated`() {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = EventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, listener.received.size)
    }
}
