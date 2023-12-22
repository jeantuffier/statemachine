plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    id("convention.publication")
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm()

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "StateMachine"
        }
    }

    js(IR) {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.arrow)
            implementation(libs.coroutines.core)
            implementation(libs.serialization)
        }

        commonTest.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.coroutines.test)
            implementation(libs.kotlin.test.common)
            implementation(libs.kotlin.test.annotation)
            implementation(libs.turbine)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.test.junit)
        }

        jsTest.dependencies {
            implementation(libs.kotlin.test.js)
        }
    }
}

// Fix Gradle warning about signing tasks using publishing task outputs without explicit dependencies
// https://youtrack.jetbrains.com/issue/KT-46466
tasks.withType<AbstractPublishToMaven>().configureEach {
    val signingTasks = tasks.withType<Sign>()
    mustRunAfter(signingTasks)
}
