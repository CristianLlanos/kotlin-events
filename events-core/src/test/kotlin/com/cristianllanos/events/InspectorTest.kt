package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InspectorTest {

    @Test
    fun `hasListeners returns false when no listeners`() {
        val bus = EventBus(FakeResolver())

        assertFalse(bus.hasListeners<UserCreated>())
    }

    @Test
    fun `hasListeners returns true for class listener`() {
        val bus = EventBus(FakeResolver())
        bus.subscribe<UserCreated, UserCreatedListener>()

        assertTrue(bus.hasListeners<UserCreated>())
    }

    @Test
    fun `hasListeners returns true for lambda listener`() {
        val bus = EventBus(FakeResolver())
        bus.on<UserCreated> { }

        assertTrue(bus.hasListeners<UserCreated>())
    }

    @Test
    fun `hasListeners returns true when global listener exists`() {
        val bus = EventBus(FakeResolver())
        bus.onAny { }

        assertTrue(bus.hasListeners<UserCreated>())
    }

    @Test
    fun `listenerCount counts class and lambda listeners`() {
        val bus = EventBus(FakeResolver())
        bus.subscribe<UserCreated, UserCreatedListener>()
        bus.on<UserCreated> { }

        assertEquals(2, bus.listenerCount<UserCreated>())
    }

    @Test
    fun `listenerCount includes global listeners`() {
        val bus = EventBus(FakeResolver())
        bus.onAny { }

        assertEquals(1, bus.listenerCount<UserCreated>())
    }

    @Test
    fun `listenerCount returns zero when empty`() {
        val bus = EventBus(FakeResolver())

        assertEquals(0, bus.listenerCount<UserCreated>())
    }

    interface Auditable : Event
    data class AuditableEvent(val name: String) : Auditable

    @Test
    fun `hasListeners walks hierarchy - parent listener detected for child event`() {
        val bus = EventBus(FakeResolver())
        bus.on<Auditable> { }

        assertTrue(bus.hasListeners<AuditableEvent>())
    }

    @Test
    fun `listenerCount walks hierarchy`() {
        val bus = EventBus(FakeResolver())
        bus.on<Auditable> { }
        bus.on<AuditableEvent> { }

        assertEquals(2, bus.listenerCount(AuditableEvent::class.java))
    }
}
