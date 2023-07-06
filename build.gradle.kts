plugins {
    kotlin("multiplatform") apply false
    id("com.diffplug.spotless")
}

subprojects {
    repositories {
        mavenCentral()
    }
}

//configure<com.diffplug.gradle.spotless.SpotlessExtension> {
//    kotlin {
//        target("**/*.kt", "**/*.kts")
//        ktlint()
//    }
//}
