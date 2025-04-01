plugins {
    id("kotlin-multiplatform-conventions")
    alias(libs.plugins.google.ksp)
    id(
        libs.plugins.kotlinx.kover
            .get()
            .pluginId,
    )
}

group = rootProject.group
version = rootProject.version

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":free-dsl-core"))
            }
        }
    }
}

// Add the free-dsl-core module as a KSP dependency
dependencies {
    add("kspJvm", project(":free-dsl-core"))
}
