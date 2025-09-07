import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    
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
        }
        jvmMain.dependencies {
            implementation("net.objecthunter:exp4j:0.4.8")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

