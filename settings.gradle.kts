pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion apply false
        id("com.rickclephas.kmp.nativecoroutines") version "0.13.3" apply false
        id("org.jmailen.kotlinter") version "3.14.0" apply false
        kotlin("multiplatform") version kotlinVersion apply false
        kotlin("plugin.serialization") version "1.8.10" apply false
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "statemachine"

include(":orchestrate")
include(":example")
include(":core")
include(":processor")
