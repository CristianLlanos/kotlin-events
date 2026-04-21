package com.cristianllanos.events

import com.cristianllanos.container.Container
import com.cristianllanos.container.resolve
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class EventServiceProviderTest {

    @Test
    fun `registers EventBus as singleton`() {
        val container = Container()
        EventServiceProvider().register(container)

        val bus1 = container.resolve<EventBus>()
        val bus2 = container.resolve<EventBus>()

        assertSame(bus1, bus2)
    }

    @Test
    fun `Emitter resolves to the same EventBus instance`() {
        val container = Container()
        EventServiceProvider().register(container)

        val bus = container.resolve<EventBus>()
        val emitter = container.resolve<Emitter>()

        assertSame(bus, emitter)
    }

    @Test
    fun `Subscriber resolves to the same EventBus instance`() {
        val container = Container()
        EventServiceProvider().register(container)

        val bus = container.resolve<EventBus>()
        val subscriber = container.resolve<Subscriber>()

        assertSame(bus, subscriber)
    }

    @Test
    fun `end-to-end subscribe and emit via container`() {
        val container = Container()
        EventServiceProvider().register(container)
        val received = mutableListOf<String>()

        val subscriber = container.resolve<Subscriber>()
        subscriber.on<UserCreated> { received.add(it.name) }

        val emitter = container.resolve<Emitter>()
        emitter.emit(UserCreated("Alice"))

        assertEquals(listOf("Alice"), received)
    }
}
