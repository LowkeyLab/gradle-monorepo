plugins {
    id("kotlin-spring-conventions")
    alias(libs.plugins.sonarqube)
}

group = "com.github.lowkeylab"
version = "0.0.1" // x-release-please-version

dependencies {
    implementation(libs.spring.boot.starterDataMongodb)
    implementation(libs.spring.boot.starterWeb)
    implementation(libs.springdoc.openApiStarterWebMvcUi)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation(libs.testcontainers.postgresql)
            }
        }
    }
}

tasks.sonar {
    dependsOn(tasks.koverXmlReport)
}

tasks.checkCI {
    dependsOn(tasks.sonar)
}

tasks.bootBuildImage {
    val image = "tacascer/${project.name}"
    imageName = image
    tags = listOf("$image:${project.version}", "$image:latest")
    if (System.getenv("DOCKER_HUB_TOKEN") != null) {
        publish = true
        docker {
            publishRegistry {
                username = "tacascer"
                password = System.getenv("DOCKER_HUB_TOKEN")
            }
        }
    }
}

tasks.releaseCI {
    dependsOn(tasks.bootBuildImage)
}

sonar {
    properties {
        property("sonar.projectKey", "lowkeylab_gradle-monorepo_guess-the-word")
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
