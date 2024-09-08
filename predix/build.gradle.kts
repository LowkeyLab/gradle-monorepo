plugins {
    alias(libs.plugins.sonarqube)
    id("kotlin-spring-conventions")
}

group = "io.github.tacascer"
version = "1.4.0" // x-release-please-version

dependencies {
    implementation(libs.liquibase.core)
    implementation(libs.mapstruct)
    implementation(libs.spring.boot.starterDataJpa)
    implementation(libs.spring.boot.starterWeb)
    implementation(libs.springdoc.openApiStarterWebMvcUi)
    kapt(libs.mapstruct.processor)
    runtimeOnly(libs.postgresql)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.testcontainers.postgresql)
            }
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "tacascer-org_gradle-monorepo_predix")
        property("sonar.organization", "tacascer-org")
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

tasks.qualityCheck {
    dependsOn(tasks.sonar)
}
