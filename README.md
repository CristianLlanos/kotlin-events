# kotlin-events

A lightweight, type-safe event bus for Kotlin with dependency-injected listeners.

## Modules

| Module | Artifact | Description |
|--------|----------|-------------|
| `events-core` | `com.cristianllanos:events:1.0.0` | Event bus with DI-resolved listeners |
| `events-coroutines` | `com.cristianllanos:events-coroutines:1.0.0` | Suspending listeners and emit |

## Installation

```kotlin
dependencies {
    implementation("com.cristianllanos:events:1.0.0")

    // Optional: coroutines support
    implementation("com.cristianllanos:events-coroutines:1.0.0")
}
```

Both pull in [`kotlin-container`](https://github.com/CristianLlanos/kotlin-container) as a transitive dependency.

## Quick start

```kotlin
val container = Container()
EventServiceProvider().register(container)

val bus = container.resolve<EventBus>()
bus.subscribe<UserCreated, SendWelcomeEmail>()
bus.emit(UserCreated("Alice"))
```

Define events as data classes and listeners with injected dependencies:

```kotlin
data class UserCreated(val name: String) : Event

class SendWelcomeEmail(private val mailer: Mailer) : Listener<UserCreated> {
    override fun handle(event: UserCreated) {
        mailer.send("Welcome, ${event.name}!")
    }
}
```

Listeners are resolved from the container on each emit, so their dependencies are auto-injected.

## Lambda listeners

Register inline handlers without creating a class:

```kotlin
val subscription = bus.on<UserCreated> { event ->
    println("New user: ${event.name}")
}

subscription.cancel() // removes the listener
```

## One-shot listeners

Listeners that fire once and auto-unsubscribe:

```kotlin
bus.once<UserCreated> { event -> println("First user: ${event.name}") }

bus.once<UserCreated, SendWelcomeEmail>() // class-based one-shot
```

## Catch-all listener

Receive every event regardless of type:

```kotlin
bus.onAny { event -> println("Event: $event") }
```

## Multiple listeners

```kotlin
bus.subscribe<UserCreated>(
    SendWelcomeEmail::class,
    AuditLogListener::class,
    AnalyticsListener::class,
)
```

## Registration DSL

Bulk-register event-listener mappings:

```kotlin
bus.register {
    UserCreated::class mappedTo listOf(SendWelcomeEmail::class, LogNewUser::class)
    OrderPlaced::class mappedTo listOf(NotifyWarehouse::class)
}
```

## Middleware

Intercept event dispatch for cross-cutting concerns:

```kotlin
bus.use { event, next ->
    val start = System.nanoTime()
    next(event) // call next to continue the pipeline
    println("Dispatched ${event::class.simpleName} in ${System.nanoTime() - start}ns")
}
```

Omit the `next` call to short-circuit dispatch.

## Inspector

Query the event bus state:

```kotlin
val bus = container.resolve<EventBus>()

bus.hasListeners<UserCreated>()   // true/false
bus.listenerCount<UserCreated>()  // number of registered listeners
```

## Error resilience

When a listener throws, remaining listeners still execute. Errors are collected and rethrown after all listeners have run. A single error is thrown directly; multiple errors are wrapped in `CompositeEventException`.

```kotlin
val bus = EventBus(container, onError = { e -> logger.error("Dispatch failed", e) })
```

## Event hierarchy

Listeners registered for a parent event type are also invoked when a subtype is emitted:

```kotlin
interface DomainEvent : Event
data class UserCreated(val name: String) : DomainEvent

bus.onAny { event -> /* receives all events */ }
bus.on<DomainEvent> { event -> /* receives UserCreated too */ }
```

## Unsubscribe and clear

```kotlin
bus.unsubscribe<UserCreated, SendWelcomeEmail>()
bus.clear()
```

## Interface segregation

```kotlin
interface Emitter     // emit()
interface Subscriber  // subscribe(), unsubscribe(), on(), once(), use(), register(), clear()
interface Inspector   // hasListeners(), listenerCount()
interface EventBus : Emitter, Subscriber, Inspector
```

Inject only what each component needs:

```kotlin
class OrderService(private val events: Emitter) {
    fun placeOrder(order: Order) {
        events.emit(OrderPlaced(order.id))
    }
}
```

## Service provider

`EventServiceProvider` registers `EventBus`, `Emitter`, and `Subscriber` as singletons:

```kotlin
val container = Container()
EventServiceProvider().register(container)
```

## Coroutines

The `events-coroutines` module provides suspending counterparts for the core interfaces:

```kotlin
val bus = SuspendingEventBus(container)

bus.subscribeSuspending<UserCreated, AsyncWelcomeEmail>()
bus.on<UserCreated> { event -> delay(100); println(event.name) }

coroutineScope { bus.emit(UserCreated("Alice")) }
```

Suspending listeners implement `SuspendingListener`:

```kotlin
class AsyncWelcomeEmail(private val mailer: SuspendingMailer) : SuspendingListener<UserCreated> {
    override suspend fun handle(event: UserCreated) {
        mailer.send("Welcome, ${event.name}!")
    }
}
```

A `SuspendingEventBus` accepts both `Listener` and `SuspendingListener` registrations.

## License

MIT
