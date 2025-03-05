plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sonarqube)
    id("kotlin-library-conventions")
}

group = "io.github.tacascer.kotlinx.serialization"
version = "0.1.0" // x-release-please-version

kotlin {
    jvm()
    
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.runnerJunit5)
                implementation(libs.kotest.property)
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "lowkeylab_gradle-monorepo_kotlinx-serialization-ber")
        property("sonar.organization", "lowkeylab")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.asFile.get()}/reports/kover/report.xml",
        )
        property(
            "sonar.userHome",
            "${layout.buildDirectory.asFile.get()}/.sonar",
        )
    }
}

tasks.sonar {
    dependsOn(tasks.koverXmlReport)
}

tasks.checkCI {
    dependsOn(tasks.sonar)
}
