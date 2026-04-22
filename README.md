# kotlin-events

A lightweight, type-safe event bus for Kotlin with DI-resolved listeners, middleware, and coroutines support.

[![Maven Central](https://img.shields.io/maven-central/v/com.cristianllanos/events)](https://central.sonatype.com/artifact/com.cristianllanos/events)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Installation

```kotlin
// Synchronous only
dependencies {
    implementation("com.cristianllanos:events:1.0.0")
}

// With coroutines support (pulls in events-core automatically)
dependencies {
    implementation("com.cristianllanos:events-coroutines:1.0.0")
}
```

## Quick Start

```kotlin
data class UserCreated(val name: String) : Event

class SendWelcomeEmail(private val mailer: Mailer) : Listener<UserCreated> {
    override fun handle(event: UserCreated) {
        mailer.send("Welcome, ${event.name}!")
    }
}

val container = Container()
EventServiceProvider().register(container)

val bus = container.resolve<EventBus>()
bus.subscribe<UserCreated, SendWelcomeEmail>()
bus.emit(UserCreated("Alice"))
```

## Documentation

- [Getting Started](https://cristianllanos.com/projects/kotlin-events/guide/) — Installation, events and listeners, DI wiring
- [Listeners](https://cristianllanos.com/projects/kotlin-events/listeners/) — Lambda handlers, one-shot, catch-all, registration DSL
- [Middleware & Errors](https://cristianllanos.com/projects/kotlin-events/middleware/) — Middleware pipeline, error resilience, inspector, event hierarchy
- [Coroutines](https://cristianllanos.com/projects/kotlin-events/coroutines/) — Suspending listeners and emit, mixed handlers, migration
- [Advanced](https://cristianllanos.com/projects/kotlin-events/advanced/) — Thread safety, interface segregation, once guarantees
- [API Reference](https://cristianllanos.com/projects/kotlin-events/api/) — Complete public API
- [Changelog](https://cristianllanos.com/projects/kotlin-events/changelog/) — Release history

## License

MIT
