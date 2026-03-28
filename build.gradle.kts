plugins {
    kotlin("jvm") version "1.9.25"
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "com.cristianllanos"
version = "0.1.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.cristianllanos:container:0.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.25")
}

signing {
    useGpgCmd()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates("com.cristianllanos", "events", version.toString())

    pom {
        name.set("Kotlin Events")
        description.set("A lightweight event bus for Kotlin with dependency-injected listeners")
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
