plugins {
    id("monorepo-conventions")
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kotlinx.kover)
}

group = "io.github.lowkeylab"
version = "1.2.1" // x-release-please-version

sonar {
    properties {
        property("sonar.projectKey", "lowkeylab_gradle-monorepo_free-dsl")
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

dependencies {
    kover(project(":free-dsl-core"))
    kover(project(":free-dsl-test"))
}

tasks.sonar {
    dependsOn(tasks.koverXmlReport)
}

tasks.checkCI {
    dependsOn(tasks.sonar)
}
