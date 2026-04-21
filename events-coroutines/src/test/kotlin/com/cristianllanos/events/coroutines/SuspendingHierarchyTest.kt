package com.cristianllanos.events.coroutines

import com.cristianllanos.events.Event
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class SuspendingHierarchyTest {

    interface Auditable : Event
    data class AuditableUserCreated(val name: String) : Auditable

    @Test
    fun `suspending listener on parent interface receives child events`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<Event>()

        bus.on<Auditable> { received.add(it) }
        bus.emit(AuditableUserCreated("Alice"))

        assertEquals(1, received.size)
    }

    @Test
    fun `both exact and parent suspending listeners fire`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val exact = mutableListOf<Event>()
        val parent = mutableListOf<Event>()

        bus.on<AuditableUserCreated> { exact.add(it) }
        bus.on<Auditable> { parent.add(it) }
        bus.emit(AuditableUserCreated("Alice"))

        assertEquals(1, exact.size)
        assertEquals(1, parent.size)
    }

    @Test
    fun `hierarchy does not trigger unrelated listeners`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<Event>()

        bus.on<UserCreated> { received.add(it) }
        bus.emit(OrderPlaced(42))

        assertEquals(0, received.size)
    }

    @Test
    fun `listener on Event interface receives all event types`() = runTest {
        val bus = SuspendingEventBus(FakeResolver())
        val received = mutableListOf<Event>()

        bus.on<Event> { received.add(it) }
        bus.emit(UserCreated("Alice"))
        bus.emit(OrderPlaced(42))
        bus.emit(AuditableUserCreated("Bob"))

        assertEquals(3, received.size)
    }
}
