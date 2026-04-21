package com.cristianllanos.events

import org.junit.Test
import kotlin.test.assertEquals

class HierarchyTest {

    interface Auditable : Event

    data class AuditableUserCreated(val name: String) : Auditable

    @Test
    fun `listener registered for parent interface receives child events`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<Event>()

        bus.on<Auditable> { received.add(it) }
        bus.emit(AuditableUserCreated("Alice"))

        assertEquals(1, received.size)
    }

    @Test
    fun `listener registered for Event interface receives all events`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<Event>()

        bus.on<Event> { received.add(it) }
        bus.emit(UserCreated("Alice"))
        bus.emit(OrderPlaced(42))

        assertEquals(2, received.size)
    }

    @Test
    fun `both exact and parent listeners fire`() {
        val bus = EventBus(FakeResolver())
        val exactReceived = mutableListOf<Event>()
        val parentReceived = mutableListOf<Event>()

        bus.on<AuditableUserCreated> { exactReceived.add(it) }
        bus.on<Auditable> { parentReceived.add(it) }
        bus.emit(AuditableUserCreated("Alice"))

        assertEquals(1, exactReceived.size)
        assertEquals(1, parentReceived.size)
    }

    @Test
    fun `hierarchy does not trigger unrelated listeners`() {
        val bus = EventBus(FakeResolver())
        val received = mutableListOf<Event>()

        bus.on<UserCreated> { received.add(it) }
        bus.emit(OrderPlaced(42))

        assertEquals(0, received.size)
    }
}
