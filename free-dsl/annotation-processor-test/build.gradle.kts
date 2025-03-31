plugins {
    id("kotlin-multiplatform-conventions")
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.sonarqube)
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
    }
}

// Add the annotation processor as a KSP dependency
dependencies {
    add("kspJvm", project(":annotation-processor"))
}

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

tasks.sonar {
    dependsOn(tasks.koverXmlReport)
}

tasks.checkCI {
    dependsOn(tasks.sonar)
}
