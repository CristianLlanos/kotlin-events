plugins {
    kotlin("jvm") version "1.9.25"
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
    id("org.jetbrains.dokka") version "1.9.20"
}

group = "com.cristianllanos"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":events-core"))
    implementation("com.cristianllanos:container:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.25")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("com.cristianllanos", "events-coroutines", version.toString())

    pom {
        name.set("Kotlin Events Coroutines")
        description.set("Coroutines extension for kotlin-events: suspending listeners and emit")
        url.set("https://github.com/CristianLlanos/kotlin-events")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("cristianllanos")
                name.set("Cristian Llanos")
                email.set("cristianllanos@outlook.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/CristianLlanos/kotlin-events.git")
            developerConnection.set("scm:git:ssh://github.com/CristianLlanos/kotlin-events.git")
            url.set("https://github.com/CristianLlanos/kotlin-events")
        }
    }
}
