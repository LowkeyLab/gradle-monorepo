plugins {
    id("kotlin-multiplatform-conventions")
    alias(libs.plugins.google.ksp) apply false
}

group = "com.github.lowkeylab"
version = "0.0.1" // x-release-please-version

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.google.ksp.symbolProcessingApi)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}
