
val kspVersion: String by project

plugins {
    kotlin("multiplatform")
    id("maven-publish")
}

group = "com.jeantuffier.statemachine"
version = "2.1.0-alpha2"

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
    jvm {
        withJava()
    }

    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":annotation"))
                implementation(project(":framework"))
                implementation("com.squareup:kotlinpoet:1.12.0")
                implementation("com.squareup:kotlinpoet-ksp:1.12.0")
                implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
    }
}