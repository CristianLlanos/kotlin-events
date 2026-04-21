package com.cristianllanos.events

import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CyclicBarrier
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThreadSafetyTest {

    @Test
    fun `concurrent subscribe and emit do not throw`() {
        val bus = EventBus(FakeResolver())
        val received = CopyOnWriteArrayList<String>()
        val barrier = CyclicBarrier(3)
        val errors = CopyOnWriteArrayList<Throwable>()

        bus.on<UserCreated> { received.add(it.name) }

        val emitter = Thread {
            try {
                barrier.await()
                repeat(100) { bus.emit(UserCreated("emit-$it")) }
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        val subscriber = Thread {
            try {
                barrier.await()
                repeat(100) { bus.on<UserCreated> { received.add(it.name) } }
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        emitter.start()
        subscriber.start()
        barrier.await()
        emitter.join()
        subscriber.join()

        assertTrue(errors.isEmpty(), "Expected no errors but got: $errors")
        assertTrue(received.isNotEmpty())
    }

    @Test
    fun `concurrent emit and cancel do not throw`() {
        val bus = EventBus(FakeResolver())
        val errors = CopyOnWriteArrayList<Throwable>()
        val barrier = CyclicBarrier(2)

        val subscriptions = (1..100).map { bus.on<UserCreated> { } }

        val emitter = Thread {
            try {
                barrier.await()
                repeat(100) { bus.emit(UserCreated("emit-$it")) }
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        val canceller = Thread {
            try {
                barrier.await()
                subscriptions.forEach { it.cancel() }
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        emitter.start()
        canceller.start()
        emitter.join()
        canceller.join()

        assertTrue(errors.isEmpty(), "Expected no errors but got: $errors")
    }

    @Test
    fun `concurrent emit and clear do not throw`() {
        val bus = EventBus(FakeResolver())
        val errors = CopyOnWriteArrayList<Throwable>()
        val latch = CountDownLatch(2)

        bus.on<UserCreated> { }
        bus.on<OrderPlaced> { }

        val emitter = Thread {
            try {
                repeat(200) { bus.emit(UserCreated("emit-$it")) }
            } catch (e: Throwable) {
                errors.add(e)
            } finally {
                latch.countDown()
            }
        }

        val clearer = Thread {
            try {
                repeat(200) {
                    bus.clear()
                    bus.on<UserCreated> { }
                }
            } catch (e: Throwable) {
                errors.add(e)
            } finally {
                latch.countDown()
            }
        }

        emitter.start()
        clearer.start()
        latch.await()

        assertTrue(errors.isEmpty(), "Expected no errors but got: $errors")
    }

    @Test
    fun `concurrent subscribe from multiple threads`() {
        val bus = EventBus(FakeResolver())
        val received = CopyOnWriteArrayList<String>()
        val barrier = CyclicBarrier(4)

        val threads = (1..3).map { threadId ->
            Thread {
                barrier.await()
                repeat(50) {
                    bus.on<UserCreated> { received.add("t$threadId") }
                }
            }
        }

        threads.forEach { it.start() }
        barrier.await()
        threads.forEach { it.join() }

        bus.emit(UserCreated("test"))

        assertEquals(150, received.size)
    }
}
