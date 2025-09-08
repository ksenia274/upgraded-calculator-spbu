import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }
    
    sourceSets {
        commonMain.dependencies {
            // put your Multiplatform dependencies here
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
        }
        jvmMain.dependencies {
            implementation("net.objecthunter:exp4j:0.4.8")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

