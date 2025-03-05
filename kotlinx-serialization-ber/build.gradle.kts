plugins {
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-multiplatform-conventions")
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
                implementation(libs.signum.indispensable)  // Use the correct reference
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
