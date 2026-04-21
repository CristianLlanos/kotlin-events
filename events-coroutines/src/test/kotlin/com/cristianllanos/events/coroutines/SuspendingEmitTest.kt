package com.cristianllanos.events.coroutines

import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SuspendingEmitTest {

    @Test
    fun `emits to suspending listener`() = runTest {
        val listener = SuspendingUserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = SuspendingEventBus(resolver)

        bus.subscribeSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, listener.received.size)
        assertEquals("Alice", listener.received[0].name)
    }

    @Test
    fun `emits to plain listener`() = runTest {
        val listener = UserCreatedListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val bus = SuspendingEventBus(resolver)

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, listener.received.size)
    }

    @Test
    fun `emits to lambda listener`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<String>()

        bus.on<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `emitting with no listeners does nothing`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        bus.emit(UserCreated("Ghost"))
    }

    @Test
    fun `cross-event isolation`() = runTest {
        val userListener = SuspendingUserCreatedListener()
        val orderListener = SuspendingOrderPlacedListener()
        val resolver = FakeResolver().apply {
            bind(userListener)
            bind(orderListener)
        }
        val bus = SuspendingEventBus(resolver)

        bus.subscribeSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.subscribeSuspending<OrderPlaced, SuspendingOrderPlacedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, userListener.received.size)
        assertEquals(0, orderListener.received.size)
    }

    @Test
    fun `vararg emit dispatches in order`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val log = mutableListOf<String>()

        bus.on<UserCreated> { log.add("user:${it.name}") }
        bus.on<OrderPlaced> { log.add("order:${it.orderId}") }
        bus.emit(UserCreated("Alice"), OrderPlaced(1))

        assertEquals(listOf("user:Alice", "order:1"), log)
    }

    @Test
    fun `mixed plain and suspending listeners`() = runTest {
        val plainListener = UserCreatedListener()
        val suspendingListener = SuspendingUserCreatedListener()
        val resolver = FakeResolver().apply {
            bind(plainListener)
            bind(suspendingListener)
        }
        val bus = SuspendingEventBus(resolver)
        val lambdaReceived = mutableListOf<String>()

        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.subscribeSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.on<UserCreated> { lambdaReceived.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(1, plainListener.received.size)
        assertEquals(1, suspendingListener.received.size)
        assertEquals(listOf("Alice"), lambdaReceived)
    }

    @Test
    fun `onAny receives all event types`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<String>()

        bus.onAny { received.add(it::class.simpleName ?: "unknown") }
        bus.emit(UserCreated("Alice"))
        bus.emit(OrderPlaced(42))

        assertEquals(listOf("UserCreated", "OrderPlaced"), received)
    }
}
