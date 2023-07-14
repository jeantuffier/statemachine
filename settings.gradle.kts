pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings
    plugins {
        id("com.google.devtools.ksp") version kspVersion apply false
        id("com.rickclephas.kmp.nativecoroutines") version "1.0.0-ALPHA-18" apply false
        id("com.diffplug.spotless") version "6.19.0" apply false
        kotlin("multiplatform") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
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
includeBuild("convention-plugins")

