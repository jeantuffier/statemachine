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

    ios {
        binaries {
            framework {
                baseName = "StateMachine"
            }
        }
    }
    iosSimulatorArm64()

    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
                implementation("io.arrow-kt:arrow-core:$arrowVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("app.cash.turbine:turbine:$turbineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutineVersion")
            }
        }

        val iosMain by getting
        val iosTest by getting {
            dependsOn(commonTest)
        }

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }

        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
    }
}

// Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://youtrack.jetbrains.com/issue/KT-46466
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
