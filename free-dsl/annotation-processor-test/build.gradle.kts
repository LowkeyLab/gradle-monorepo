plugins {
    id("kotlin-multiplatform-conventions")
    alias(libs.plugins.google.ksp)
}

group = rootProject.group
version = rootProject.version

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":annotation-processor"))
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}

// Add the annotation processor as a KSP dependency
dependencies {
    add("kspJvm", project(":annotation-processor"))
}
