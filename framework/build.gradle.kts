plugins {
    kotlin("multiplatform")
    id("maven-publish")
    id("com.rickclephas.kmp.nativecoroutines")
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

    ios {
        binaries {
            framework {
                baseName = "StateMachine"
            }
        }
    }

    js(IR) {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("io.arrow-kt:arrow-core:1.1.3")
            }
        }
        val commonTest by getting

        val iosMain by getting
        val iosTest by getting

        val jvmMain by getting
        val jvmTest by getting
    }
}
