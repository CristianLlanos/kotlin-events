# Module events

A lightweight, type-safe event bus for Kotlin with dependency-injected listeners.

# Package com.cristianllanos.events

Core event bus API. Define events by implementing [Event], handle them with [Listener],
and wire everything together via [EventBus] or the individual [Emitter] / [Subscriber] interfaces.

## Quick start

```kotlin
val bus = EventBus(container)
bus.subscribe<UserCreated, SendWelcomeEmail>()
bus.emit(UserCreated("Alice"))
```

## Lambda listeners

```kotlin
bus.on<UserCreated> { event -> println(event.name) }
bus.once<UserCreated> { event -> println("first: ${event.name}") }
bus.onAny { event -> println(event) }
```

## Middleware

```kotlin
bus.use { event, next ->
    println("before")
    next(event)
    println("after")
}
```

## With kotlin-container

```kotlin
val container = Container()
EventServiceProvider().register(container)

val emitter = container.resolve<Emitter>()
emitter.emit(UserCreated("Alice"))
```
