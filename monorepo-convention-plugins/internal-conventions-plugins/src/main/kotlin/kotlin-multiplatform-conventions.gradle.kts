import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("kotlin-conventions")
    id("org.jetbrains.kotlin.multiplatform")
}

val libs = the<LibrariesForLibs>()

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    
    targets.configureEach {
        compilations.configureEach {
            kotlinOptions {
                // Apply common Kotlin compiler options for all targets
                allWarningsAsErrors = true
                freeCompilerArgs = listOf("-Xcontext-receivers")
            }
        }
    }
    
    // Common source sets configuration
    sourceSets {
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
            }
        }
    }

}
