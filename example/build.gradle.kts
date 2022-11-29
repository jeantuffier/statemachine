plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

group = "com.jeantuffier"
version = "2.1.0-alpha2"

repositories {
    google()
    mavenCentral()
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
        binaries.executable()
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":annotation"))
                implementation(project(":framework"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("io.arrow-kt:arrow-core:1.1.3")
            }
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":framework"))
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
                implementation("app.cash.turbine:turbine:0.7.0")
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
//    add("kspJvm", project(":processor"))
//    add("kspIosArm64", project(":processor"))
//    add("kspIosX64", project(":processor"))
}
