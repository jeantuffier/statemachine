plugins {
    kotlin("multiplatform") apply false
    id("com.diffplug.spotless")
}

subprojects {
    repositories {
        mavenCentral()
    }
}
