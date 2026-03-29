# kotlin-events

A lightweight event bus for Kotlin. Type-safe publish-subscribe with dependency-injected listeners.

## Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.cristianllanos:events:0.2.1")
}
```

This pulls in [`kotlin-container`](https://github.com/CristianLlanos/kotlin-container) automatically as a transitive dependency.

## Quick start

Register the event service provider and define your events and listeners:

```kotlin
val container = Container()
container.register(EventServiceProvider())
```

Define an event and a listener:

```kotlin
class UserCreated(val name: String) : Event()

class WelcomeEmailListener(
    val emailService: EmailService,
) : Listener<UserCreated> {
    override fun handle(event: UserCreated) {
        emailService.sendWelcome(event.name)
    }
}
```

Subscribe and emit:

```kotlin
val subscriber = container.resolve<Subscriber>()
subscriber.subscribe<UserCreated, WelcomeEmailListener>()

val emitter = container.resolve<Emitter>()
emitter.emit(UserCreated("Alice"))
```

Listeners are resolved from the container, so their dependencies are auto-injected.

## Multiple listeners

Subscribe multiple listeners to an event at once:

```kotlin
bus.subscribe<UserCreated>(
    WelcomeEmailListener::class,
    AuditLogListener::class,
    AnalyticsListener::class,
)

bus.emit(UserCreated("Alice")) // all three listeners are called
```

Or one at a time:

```kotlin
bus.subscribe<UserCreated, WelcomeEmailListener>()
bus.subscribe<UserCreated, AuditLogListener>()
```

## Service provider

The library includes an `EventServiceProvider` that registers the `EventBus`, `Emitter`, and `Subscriber` as singletons in the container:

```kotlin
val container = Container()
container.register(EventServiceProvider())
```

Then wire your event subscriptions in your own service providers. The `register` method's parameters are auto-resolved from the container:

```kotlin
class OrderEventProvider {
    fun register(subscriber: Subscriber) {
        subscriber.subscribe<OrderPlaced>(
            InventoryListener::class,
            NotificationListener::class,
        )
    }
}
```

## Unsubscribe and clear

```kotlin
bus.unsubscribe<UserCreated, WelcomeEmailListener>()

bus.clear() // removes all subscriptions
```

## Interface segregation

The event bus is split into focused interfaces:

```kotlin
interface Emitter     // emit()
interface Subscriber  // subscribe(), unsubscribe(), clear()
interface EventBus : Emitter, Subscriber
```

Give each part of your code only what it needs:

```kotlin
// Services that fire events only need Emitter
class OrderService(private val events: Emitter) {
    fun placeOrder(order: Order) {
        // ...
        events.emit(OrderPlaced(order.id))
    }
}

// Setup code uses Subscriber to wire listeners
fun bootstrap(subscriber: Subscriber) {
    subscriber.subscribe<OrderPlaced>(
        InventoryListener::class,
        NotificationListener::class,
    )
}
```

## License

MIT
