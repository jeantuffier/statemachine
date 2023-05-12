val arrowVersion: String by project
val kotlinxCoroutineVersion: String by project
val turbineVersion: String by project

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
    id("org.jmailen.kotlinter")
}

group = "com.jeantuffier"
version = "0.2.0-dev9"

repositories {
    google()
    mavenCentral()
}

kotlin {
    jvm()

    ios {
        binaries {
            framework {
                baseName = "StateMachine"
            }
        }
    }

    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":orchestrate"))
                implementation(project(":core"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
                implementation("io.arrow-kt:arrow-core:$arrowVersion")
            }
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin/")
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":core"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("app.cash.turbine:turbine:$turbineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutineVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutineVersion")
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

        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", project(":processor"))
}

tasks.named("build") {
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
}

tasks.named("compileKotlinJvm") {
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
    dependsOn(tasks.named("formatKotlinGeneratedByKspCommonMainKotlinMetadata"))
}
