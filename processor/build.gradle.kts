val kspVersion: String by project
val kotlinPoetVersion: String by project
val arrowVersion: String by project
val kotlinxCoroutineVersion: String by project
val kotlinxSerializationVersion: String by project
val turbineVersion: String by project

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("convention.publication")
}

group = "com.jeantuffier.statemachine"
version = "0.2.0-dev13"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":orchestrate"))
                implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
                implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
                implementation("io.arrow-kt:arrow-core:$arrowVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}

// Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://youtrack.jetbrains.com/issue/KT-46466
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
