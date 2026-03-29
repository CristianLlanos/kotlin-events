# Module events

A lightweight, type-safe event bus for Kotlin with dependency-injected listeners.

# Package com.cristianllanos.events

Core event bus API. Define events by extending [Event], handle them with [Listener],
and wire everything together via [EventBus] or the individual [Emitter] / [Subscriber] interfaces.

## Quick start

```kotlin
// 1. Define an event
class UserCreated(val name: String) : Event()

// 2. Define a listener
class SendWelcomeEmail(private val mailer: Mailer) : Listener<UserCreated> {
    override fun handle(event: UserCreated) {
        mailer.send("Welcome, ${event.name}!")
    }
}

// 3. Wire and emit
val bus = EventBus(container)
bus.subscribe<UserCreated, SendWelcomeEmail>()
bus.emit(UserCreated("Alice"))
```

## With kotlin-container

```kotlin
val container = Container()
EventServiceProvider().register(container)

val subscriber = container.resolve<Subscriber>()
subscriber.subscribe<UserCreated, SendWelcomeEmail>()

val emitter = container.resolve<Emitter>()
emitter.emit(UserCreated("Alice"))
```
