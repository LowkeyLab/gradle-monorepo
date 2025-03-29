import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("kotlin-conventions")
    kotlin("multiplatform")
}

val libs = the<LibrariesForLibs>()

kotlin {
    jvmToolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    compilerOptions {
        allWarningsAsErrors = true
    }

    // Common source sets configuration
    sourceSets {
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotest.runnerJunit5)
            }
        }
    }
}
