plugins {
    id("kotlin-multiplatform-conventions")
    alias(libs.plugins.google.ksp)
}

group = "com.github.lowkeylab"
version = "0.0.1" // x-release-please-version

kotlin {
    jvm()
    sourceSets {
        commonMain {
        }
    }
}
