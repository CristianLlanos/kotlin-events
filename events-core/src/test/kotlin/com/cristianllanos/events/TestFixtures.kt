package com.cristianllanos.events

data class UserCreated(val name: String) : Event
data class OrderPlaced(val orderId: Int) : Event

class UserCreatedListener : Listener<UserCreated> {
    val received = mutableListOf<UserCreated>()

    override fun handle(event: UserCreated) {
        received.add(event)
    }
}

class AnotherUserCreatedListener : Listener<UserCreated> {
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

class FailingListener : Listener<UserCreated> {
    override fun handle(event: UserCreated) {
        throw RuntimeException("Listener failed for ${event.name}")
    }
}
