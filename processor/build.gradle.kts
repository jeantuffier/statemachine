
val kspVersion: String by project

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("org.jmailen.kotlinter")
}

group = "com.jeantuffier.statemachine"
version = "0.2.0-dev2"

repositories {
    google()
    mavenCentral()
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/jeantuffier/statemachine")
            name = "github"
            credentials(PasswordCredentials::class)
        }
    }
}

kotlin {
    jvm()

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":core"))
                implementation(project(":orchestrate"))
                implementation("com.squareup:kotlinpoet:1.12.0")
                implementation("com.squareup:kotlinpoet-ksp:1.12.0")
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
                implementation("io.arrow-kt:arrow-core:1.1.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}