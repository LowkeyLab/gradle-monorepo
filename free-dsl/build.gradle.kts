plugins {
    id("kotlin-multiplatform-conventions")
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
    }
}
