group = "com.jeantuffier"
version = "0.1.0-SNAPSHOT"

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
        withJava()
    }

    ios()

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("src/main/kotlin")
            resources.srcDir("src/main/resources")
        }
        val commonTest by getting
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