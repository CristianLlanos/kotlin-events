package com.cristianllanos.events.coroutines

import com.cristianllanos.container.Container
import com.cristianllanos.container.resolve
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class SuspendingEventServiceProviderTest {

    @Test
    fun `registers SuspendingEventBus as singleton`() {
        val container = Container()
        SuspendingEventServiceProvider().register(container)

        val bus1 = container.resolve<SuspendingEventBus>()
        val bus2 = container.resolve<SuspendingEventBus>()

        assertSame(bus1, bus2)
    }

    @Test
    fun `SuspendingEmitter resolves to the same bus instance`() {
        val container = Container()
        SuspendingEventServiceProvider().register(container)

        val bus = container.resolve<SuspendingEventBus>()
        val emitter = container.resolve<SuspendingEmitter>()

        assertSame(bus, emitter)
    }

    @Test
    fun `SuspendingSubscriber resolves to the same bus instance`() {
        val container = Container()
        SuspendingEventServiceProvider().register(container)

        val bus = container.resolve<SuspendingEventBus>()
        val subscriber = container.resolve<SuspendingSubscriber>()

        assertSame(bus, subscriber)
    }

    @Test
    fun `end-to-end subscribe and emit via container`() = runTest {
        val container = Container()
        SuspendingEventServiceProvider().register(container)
        val received = mutableListOf<String>()

        val subscriber = container.resolve<SuspendingSubscriber>()
        subscriber.on<UserCreated> { received.add(it.name) }

        val emitter = container.resolve<SuspendingEmitter>()
        emitter.emit(UserCreated("Alice"))

        assertEquals(listOf("Alice"), received)
    }
}
