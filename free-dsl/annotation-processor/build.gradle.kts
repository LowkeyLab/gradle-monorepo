plugins {
    id("kotlin-multiplatform-conventions")
    alias(libs.plugins.google.ksp) apply false
}

group = rootProject.group
version = rootProject.version

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.google.ksp.symbolProcessingApi)
                implementation(libs.square.kotlinPoet)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}
