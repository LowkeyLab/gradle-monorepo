import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("kotlin-conventions")
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.kapt")
    `jvm-test-suite`
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.oshai.kotlinLoggingJvm)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xjsr305=strict")
    }
    jvmToolchain(21)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.kotest.property)
                implementation(libs.kotest.runnerJunit5)
                implementation(libs.testcontainers.junitJupiter)
            }
        }
    }
}
