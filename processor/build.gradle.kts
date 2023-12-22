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
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":orchestrate"))

                implementation(libs.arrow)
                implementation(libs.coroutines.core)
                implementation(libs.ksp)
                implementation(libs.serialization)

                implementation(libs.bundles.kotlin.poet)
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
