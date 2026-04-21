package com.cristianllanos.events.coroutines

import com.cristianllanos.events.Event
import com.cristianllanos.events.Listener

data class UserCreated(val name: String) : Event
data class OrderPlaced(val orderId: Int) : Event

class UserCreatedListener : Listener<UserCreated> {
    val received = mutableListOf<UserCreated>()

    override fun handle(event: UserCreated) {
        received.add(event)
    }
}

class OrderPlacedListener : Listener<OrderPlaced> {
    val received = mutableListOf<OrderPlaced>()

    override fun handle(event: OrderPlaced) {
        received.add(event)
    }
}

class SuspendingUserCreatedListener : SuspendingListener<UserCreated> {
    val received = mutableListOf<UserCreated>()

    override suspend fun handle(event: UserCreated) {
        received.add(event)
    }
}

class SuspendingOrderPlacedListener : SuspendingListener<OrderPlaced> {
    val received = mutableListOf<OrderPlaced>()

    override suspend fun handle(event: OrderPlaced) {
        received.add(event)
    }
}
