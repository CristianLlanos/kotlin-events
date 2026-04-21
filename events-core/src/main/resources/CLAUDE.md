# Kotlin Events

A lightweight, type-safe event bus for Kotlin with dependency-injected listeners.

Coordinates: `com.cristianllanos:events` (core), `com.cristianllanos:events-coroutines` (coroutines)

## Core concepts

- **Event**: Interface. Implement it to define domain events (`data class UserCreated(val name: String) : Event`).
- **Listener<T>**: Interface with `handle(event: T)`. Listeners are resolved from the DI container on each emit.
- **Emitter**: Fires events via `emit(event)`. Supports vararg emit for multiple events.
- **Subscriber**: Registers listeners. Supports class-based (`subscribe`), lambda (`on`), one-shot (`once`), catch-all (`onAny`), middleware (`use`), and DSL (`register`) registration.
- **Inspector**: Introspection via `hasListeners<E>()` and `listenerCount<E>()`.
- **EventBus**: Combines Emitter + Subscriber + Inspector. Created via `EventBus(resolver)` factory.
- **Middleware**: Intercepts dispatch pipeline. Call `next(event)` to continue or omit to short-circuit.
- **Subscription**: Handle returned by lambda registrations. Call `cancel()` to remove.
- **EventServiceProvider**: Registers EventBus, Emitter, and Subscriber as singletons in a kotlin-container `Container`.

## Thread safety

EventBus and SuspendingEventBus are thread-safe. All mutable state is guarded by synchronized blocks. Dispatch uses snapshot-based iteration so concurrent subscribe/unsubscribe/clear during emit is safe. One-shot listeners (`once()`) are guaranteed to fire at most once, even under concurrent or reentrant emit.

## Usage patterns

### Standalone

```kotlin
val bus = EventBus(resolver)
bus.subscribe<UserCreated, SendWelcomeEmail>()
bus.on<UserCreated> { println(it.name) }
bus.emit(UserCreated("Alice"))
```

### With kotlin-container

```kotlin
val container = Container()
EventServiceProvider().register(container)
val emitter = container.resolve<Emitter>()
emitter.emit(UserCreated("Alice"))
```

### Lambda and one-shot listeners

```kotlin
val sub = bus.on<UserCreated> { println(it.name) }
sub.cancel()

bus.once<UserCreated> { println("first only: ${it.name}") }
bus.onAny { println("all events: $it") }
```

### Middleware

```kotlin
bus.use { event, next ->
    val start = System.nanoTime()
    next(event)
    println("${event::class.simpleName} in ${System.nanoTime() - start}ns")
}
```

### Registration DSL

```kotlin
bus.register {
    UserCreated::class mappedTo listOf(SendWelcomeEmail::class, LogNewUser::class)
    OrderPlaced::class mappedTo listOf(NotifyWarehouse::class)
}
```

### Error handling

```kotlin
val bus = EventBus(resolver, onError = { e -> logger.error("dispatch failed", e) })
```

In the coroutines module, `onError` is a suspend function:

```kotlin
val bus = SuspendingEventBus(resolver, onError = { e -> errorChannel.send(e) })
```

### Coroutines (events-coroutines module)

```kotlin
val bus = SuspendingEventBus(container)
bus.subscribeSuspending<UserCreated, AsyncWelcomeEmail>()
bus.on<UserCreated> { delay(100); println(it.name) }
coroutineScope { bus.emit(UserCreated("Alice")) }
```

## API reference

| Type | Role |
|------|------|
| `Event` | Interface for all events |
| `Listener<T>` | Synchronous handler |
| `SuspendingListener<T>` | Suspending handler (coroutines module) |
| `Emitter` | `emit(event)`, `emit(first, vararg rest)` |
| `Subscriber` | `subscribe`, `unsubscribe`, `on`, `once`, `onAny`, `use`, `register`, `clear` |
| `Inspector` | `hasListeners<E>()`, `listenerCount<E>()` |
| `EventBus` | Emitter + Subscriber + Inspector |
| `SuspendingEventBus` | SuspendingEmitter + SuspendingSubscriber + Inspector |
| `Middleware` | `handle(event, next)` |
| `Subscription` | `cancel()` |
| `CompositeEventException` | Wraps multiple listener errors |
| `EventServiceProvider` | Registers singletons in Container |
