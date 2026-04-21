package com.cristianllanos.events.coroutines

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SuspendingSubscriberTest {

    @Test
    fun `unsubscribeSuspending removes a listener`() = runTest {
        val listener = SuspendingUserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = SuspendingEventBus(resolver)

        bus.subscribeSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.unsubscribeSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(0, listener.received.size)
    }

    @Test
    fun `off removes a lambda listener`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<String>()

        val subscription = bus.on<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))
        subscription.cancel()
        bus.emit(UserCreated("Bob"))

        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `clear removes all listeners`() = runTest {
        val userListener = SuspendingUserCreatedListener()
        val orderListener = SuspendingOrderPlacedListener()
        val resolver = FakeResolver().apply {
            bind(userListener)
            bind(orderListener)
        }
        val bus = SuspendingEventBus(resolver)

        bus.subscribeSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.subscribeSuspending<OrderPlaced, SuspendingOrderPlacedListener>()
        bus.clear()
        bus.emit(UserCreated("Alice"))
        bus.emit(OrderPlaced(1))

        assertEquals(0, userListener.received.size)
        assertEquals(0, orderListener.received.size)
    }

    @Test
    fun `bulk subscribe suspending listeners`() = runTest {
        val listener = SuspendingUserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = SuspendingEventBus(resolver)

        bus.subscribeSuspending<UserCreated>(SuspendingUserCreatedListener::class)
        bus.emit(UserCreated("Alice"))

        assertEquals(1, listener.received.size)
    }

    @Test
    fun `subscribe returns subscriber for chaining`() {
        val bus = SuspendingEventBus(FakeResolver())

        bus.subscribe<UserCreated, UserCreatedListener>()
            .subscribeSuspending<OrderPlaced, SuspendingOrderPlacedListener>()
    }

    @Test
    fun `onAny subscription can be cancelled`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<String>()

        val subscription = bus.onAny { received.add("any") }
        bus.emit(UserCreated("Alice"))
        subscription.cancel()
        bus.emit(UserCreated("Bob"))

        assertEquals(listOf("any"), received)
    }
}
