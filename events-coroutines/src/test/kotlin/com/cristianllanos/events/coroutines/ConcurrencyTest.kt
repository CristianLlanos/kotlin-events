package com.cristianllanos.events.coroutines

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.assertEquals

class ConcurrencyTest {

    @Test
    fun `concurrent emit from multiple coroutines does not throw`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = CopyOnWriteArrayList<String>()

        bus.on<UserCreated> { received.add(it.name) }

        val jobs = (1..10).map { i ->
            launch {
                repeat(50) { j -> bus.emit(UserCreated("c$i-e$j")) }
            }
        }
        jobs.forEach { it.join() }

        assertEquals(500, received.size)
    }

    @Test
    fun `concurrent subscribe and emit do not throw`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = CopyOnWriteArrayList<String>()

        val emitter = launch {
            repeat(100) { bus.emit(UserCreated("emit-$it")) }
        }

        val subscriber = launch {
            repeat(100) { bus.on<UserCreated> { received.add(it.name) } }
        }

        emitter.join()
        subscriber.join()
    }

    @Test
    fun `once fires at most once under concurrent emit`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = CopyOnWriteArrayList<String>()

        bus.once<UserCreated> { received.add(it.name) }

        val jobs = (1..10).map { i ->
            launch { bus.emit(UserCreated("coroutine-$i")) }
        }
        jobs.forEach { it.join() }

        assertEquals(1, received.size)
    }

    @Test
    fun `once does not fire on reentrant suspending emit`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
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
    fun `concurrent emit and clear do not throw`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())

        bus.on<UserCreated> { }

        val emitter = launch {
            repeat(100) { bus.emit(UserCreated("emit-$it")) }
        }

        val clearer = launch {
            repeat(100) {
                bus.clear()
                bus.on<UserCreated> { }
            }
        }

        emitter.join()
        clearer.join()
    }

    @Test
    fun `suspend onError handler is invoked`() = runTest {
        val errors = CopyOnWriteArrayList<Throwable>()
        val bus = SuspendingEventBus(FakeResolver(), onError = { errors.add(it) })

        bus.on<UserCreated> { throw RuntimeException("fail") }
        bus.emit(UserCreated("Alice"))

        assertEquals(1, errors.size)
    }

    @Test
    fun `concurrent cancel and emit do not throw`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val subscriptions = (1..50).map { bus.on<UserCreated> { } }

        val emitter = launch {
            repeat(100) { bus.emit(UserCreated("emit-$it")) }
        }

        val canceller = launch {
            subscriptions.forEach { it.cancel() }
        }

        emitter.join()
        canceller.join()
    }
}
