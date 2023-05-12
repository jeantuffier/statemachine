val kspVersion: String by project
val kotlinPoetVersion: String by project
val arrowVersion: String by project
val kotlinxCoroutineVersion: String by project
val turbineVersion: String by project

plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("org.jmailen.kotlinter")
}

group = "com.jeantuffier.statemachine"
version = "0.2.0-dev9"

repositories {
    google()
    mavenCentral()
}

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/jeantuffier/statemachine")
            name = "github"
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_PASSWORD")
            }
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
                implementation("com.squareup:kotlinpoet:$kotlinPoetVersion")
                implementation("com.squareup:kotlinpoet-ksp:$kotlinPoetVersion")
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
                implementation("io.arrow-kt:arrow-core:$arrowVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}