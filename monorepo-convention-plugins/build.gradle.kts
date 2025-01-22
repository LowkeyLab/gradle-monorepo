plugins {
    base
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kotlinx.kover)
}

version = "0.0.1" // x-release-please-version

// Duplicating functionality of project convention plugin because it is not available in the monorepo-convention-plugins project.
val lint by tasks.registering { }

val lintCI by tasks.registering {
    dependsOn(lint)
}

val checkCI by tasks.registering {
    dependsOn(subprojects.map { "${it.name}:check" })
}

val buildCI by tasks.registering { dependsOn(checkCI) }

val release by tasks.registering {}

val releaseCI by tasks.registering {}

dependencies {
    kover(project(":settings-convention-plugin"))
}

sonar {
    properties {
        property("sonar.projectKey", "lowkeylab_gradle-monorepo_monorepo-convention-plugins")
        property("sonar.organization", "lowkeylab")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.asFile.get()}/reports/kover/report.xml",
        )
        property(
            "sonar.coverage.exclusions",
            "**/MonorepoSettingsPlugin.kt",
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

checkCI {
    dependsOn(tasks.sonar)
}
