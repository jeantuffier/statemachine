pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "1.9.21-1.0.16" apply false
        id("com.rickclephas.kmp.nativecoroutines") version "1.0.0-ALPHA-23" apply false
        kotlin("multiplatform") version "1.9.21" apply false
        kotlin("plugin.serialization") version "1.9.21" apply false
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

