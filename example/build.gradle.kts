plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.ksp)
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm()

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "StateMachine"
        }
    }

    js(IR) {
        browser()
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":orchestrate"))
                implementation(project(":core"))
                implementation(libs.arrow)
                implementation(libs.coroutines.core)
            }
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin/")
        }

        commonTest.dependencies {
            implementation(project(":core"))
            implementation(libs.coroutines.core)
            implementation(libs.coroutines.test)
            implementation(libs.kotlin.test.common)
            implementation(libs.kotlin.test.annotation)
            implementation(libs.turbine)
        }

        jvmTest.dependencies {
            implementation(libs.kotlin.test.junit)
        }

        jsTest.dependencies {
            implementation(libs.kotlin.test.js)
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

tasks.named("compileKotlinIosSimulatorArm64") {
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
}

tasks.named("compileKotlinIosArm64") {
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
}

tasks.named("compileKotlinIosX64") {
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
}

tasks.named("compileKotlinJs") {
    dependsOn(tasks.named("kspCommonMainKotlinMetadata"))
}
