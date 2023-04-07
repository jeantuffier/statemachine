plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

group = "com.jeantuffier"
version = "0.1.0-dev5"

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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("io.arrow-kt:arrow-core:1.1.3")
            }
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":core"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("app.cash.turbine:turbine:0.12.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
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
}
