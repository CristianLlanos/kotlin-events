package com.cristianllanos.events.coroutines

import com.cristianllanos.container.Resolver
import com.cristianllanos.events.CompositeEventException
import com.cristianllanos.events.Event
import com.cristianllanos.events.Listener
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class SuspendingErrorResilienceTest {

    @Test
    fun `suspending listener error is collected`() = runTest {
        val listener = FailingSuspendingListener()
        val resolver = FakeResolver().apply { bind(listener) }
        val errors = mutableListOf<Throwable>()
        val bus = SuspendingEventBus(resolver, onError = { errors.add(it) })

        bus.subscribeSuspending<UserCreated, FailingSuspendingListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, errors.size)
        assertEquals("suspending fail for Alice", errors[0].message)
    }

    @Test
    fun `remaining listeners run after suspending listener throws`() = runTest {
        val failing = FailingSuspendingListener()
        val healthy = SuspendingUserCreatedListener()
        val resolver = FakeResolver().apply {
            bind(failing)
            bind(healthy)
        }
        val errors = mutableListOf<Throwable>()
        val bus = SuspendingEventBus(resolver, onError = { errors.add(it) })

        bus.subscribeSuspending<UserCreated, FailingSuspendingListener>()
        bus.subscribeSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, healthy.received.size)
        assertEquals(1, errors.size)
    }

    @Test
    fun `mixed plain and suspending errors are wrapped in CompositeEventException`() = runTest {
        val failingPlain = FailingPlainListener()
        val failingSuspending = FailingSuspendingListener()
        val resolver = FakeResolver().apply {
            bind(failingPlain)
            bind(failingSuspending)
        }
        val bus = SuspendingEventBus(resolver)

        bus.subscribe<UserCreated, FailingPlainListener>()
        bus.subscribeSuspending<UserCreated, FailingSuspendingListener>()

        val error = assertFailsWith<CompositeEventException> {
            bus.emit(UserCreated("Alice"))
        }
        assertEquals(2, error.errors.size)
    }

    @Test
    fun `lambda error does not prevent other lambdas from firing`() = runTest {
        val errors = mutableListOf<Throwable>()
        val received = mutableListOf<String>()
        val bus = SuspendingEventBus(FakeResolver(), onError = { errors.add(it) })

        bus.on<UserCreated> { throw RuntimeException("lambda fail") }
        bus.on<UserCreated> { received.add(it.name) }
        bus.emit(UserCreated("Alice"))

        assertEquals(1, errors.size)
        assertEquals(listOf("Alice"), received)
    }

    @Test
    fun `resolver error does not prevent other listeners from firing`() = runTest {
        val resolver = object : Resolver {
            val healthy = SuspendingUserCreatedListener()
            private var callCount = 0

            @Suppress("UNCHECKED_CAST")
            override fun <T : Any> resolve(type: Class<T>): T {
                callCount++
                if (callCount == 1) throw IllegalStateException("no binding")
                return healthy as T
            }
        }
        val errors = mutableListOf<Throwable>()
        val bus = SuspendingEventBus(resolver, onError = { errors.add(it) })

        bus.subscribeSuspending<UserCreated, FailingSuspendingListener>()
        bus.subscribeSuspending<UserCreated, SuspendingUserCreatedListener>()
        bus.emit(UserCreated("Alice"))

        assertEquals(1, errors.size)
        assertEquals(1, resolver.healthy.received.size)
    }

    @Test
    fun `suspend onError can perform async work`() = runTest {
        val errors = mutableListOf<String>()
        val bus = SuspendingEventBus(FakeResolver(), onError = { e ->
            kotlinx.coroutines.delay(1)
            errors.add("logged: ${e.message}")
        })

        bus.on<UserCreated> { throw RuntimeException("fail") }
        bus.emit(UserCreated("Alice"))

        assertEquals(listOf("logged: fail"), errors)
    }
}

private class FailingSuspendingListener : SuspendingListener<UserCreated> {
    override suspend fun handle(event: UserCreated) {
        throw RuntimeException("suspending fail for ${event.name}")
    }
}

private class FailingPlainListener : Listener<UserCreated> {
    override fun handle(event: UserCreated) {
        throw RuntimeException("plain fail for ${event.name}")
    }
}
