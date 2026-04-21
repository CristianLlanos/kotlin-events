# Kotlin Events

Multi-module event bus: `events-core` and `events-coroutines`.

## Publishing to Maven Central

```bash
publish-maven --dry-run    # test locally (~/.m2)
publish-maven              # publish to Maven Central (manual release via sonatype)
publish-maven --release    # publish and release automatically
```

Coordinates:
- `com.cristianllanos:events:<version>` (events-core)
- `com.cristianllanos:events-coroutines:<version>` (events-coroutines)

Version is set in each module's `build.gradle.kts`. Bump both and update the README installation snippet before publishing a new release.
