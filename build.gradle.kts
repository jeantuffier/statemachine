plugins {
    kotlin("multiplatform") version "1.6.10"
    id("com.rickclephas.kmp.nativecoroutines") version "0.12.2"
    id("maven-publish")
}

group = "com.jeantuffier"
version = "2.0.1"

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

    ios {
        binaries {
            framework {
                baseName = "SharedModels"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1-native-mt")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("app.cash.turbine:turbine:0.7.0")
            }
        }

        val iosMain by getting
        val iosTest by getting

        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
