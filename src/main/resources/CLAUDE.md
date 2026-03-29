# Kotlin Events

A lightweight, type-safe event bus for Kotlin with dependency-injected listeners.

Coordinates: `com.cristianllanos:events`

## Core concepts

- **Event**: Base class. Extend it to define domain events (`class UserCreated(val name: String) : Event()`).
- **Listener<T>**: Interface with a single `handle(event: T)` method. Listeners are resolved from the DI container on each emit, so they support constructor injection.
- **Emitter**: Fires events to registered listeners via `emit(event)`.
- **Subscriber**: Registers/unregisters listener classes for event types. Supports method chaining.
- **EventBus**: Combines Emitter + Subscriber. Created via `EventBus(resolver)` factory function.
- **EventServiceProvider**: Registers EventBus, Emitter, and Subscriber as singletons in a kotlin-container `Container`.

## Usage patterns

### Standalone (without DI container)

```kotlin
val bus = EventBus(resolver)
bus.subscribe<UserCreated, SendWelcomeEmail>()
bus.emit(UserCreated("Alice"))
```

### With kotlin-container

```kotlin
val container = Container()
EventServiceProvider().register(container)

// Inject only what you need
val emitter = container.resolve<Emitter>()
emitter.emit(UserCreated("Alice"))
```

### Multiple listeners

```kotlin
subscriber.subscribe<UserCreated>(
    SendWelcomeEmail::class,
    LogNewUser::class,
    UpdateAnalytics::class
)
```

### Interface segregation

Prefer injecting `Emitter` or `Subscriber` individually over `EventBus` for better separation of concerns.

## API reference

| Type | Role |
|------|------|
| `Event` | Base class for all events |
| `Listener<T : Event>` | Handles events of type T |
| `Emitter` | `emit(event)` |
| `Subscriber` | `subscribe(event, listener)`, `unsubscribe(event, listener)`, `clear()` |
| `EventBus` | Combines Emitter + Subscriber |
| `EventServiceProvider` | Registers singletons in a Container |

Reified extension functions on `Subscriber` avoid `.java` class references:
- `subscribe<E, L>()` / `subscribe<E>(vararg listeners)`
- `unsubscribe<E, L>()`
