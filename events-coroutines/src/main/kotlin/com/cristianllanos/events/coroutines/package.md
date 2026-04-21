# Module events-coroutines

Coroutines extension for kotlin-events: suspending listeners and emit.

# Package com.cristianllanos.events.coroutines

Suspending counterparts for the core event bus interfaces. Accepts both
[com.cristianllanos.events.Listener] and [SuspendingListener] registrations.

## Quick start

```kotlin
val bus = SuspendingEventBus(container)
bus.subscribeSuspending<UserCreated, AsyncWelcomeEmail>()
bus.on<UserCreated> { event -> delay(100); println(event.name) }

coroutineScope { bus.emit(UserCreated("Alice")) }
```

## With kotlin-container

```kotlin
val container = Container()
SuspendingEventServiceProvider().register(container)

val emitter = container.resolve<SuspendingEmitter>()
coroutineScope { emitter.emit(UserCreated("Alice")) }
```
