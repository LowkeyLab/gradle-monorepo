plugins {
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.vanniktech.mavenPublish)
    id("kotlin-multiplatform-conventions")
}

group = "io.github.lowkeylab"
version = "0.1.0" // x-release-please-version

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.datetime) // Use version from catalog
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.property)
                implementation(libs.signum.indispensable) // Use the correct reference
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kotest.runnerJunit5)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    pom {
        name.set("Kotlinx Serialization BER")
        description.set("A Kotlin multiplatform library for encoding and decoding BER (Basic Encoding Rules) data.")
        url = "https://github.com/LowkeyLab/gradle-monorepo"
        licenses {
            license {
                name = "Affero GPL v3.0"
                url = "https://opensource.org/licenses/AGPL-3.0"
            }
        }
        developers {
            developer {
                id.set("tacascer")
                name.set("Tim Tran")
                url.set("https://github.com/tacascer")
            }
        }
        scm {
            url.set("https://github.com/LowkeyLab/gradle-monorepo")
            connection.set("scm:git:git@github.com:LowkeyLab/gradle-monorepo.git")
            developerConnection.set("scm:git:git@github.com:LowkeyLab/gradle-monorepo.git")
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

tasks.releaseCI {
    dependsOn(tasks.named("publishAndReleaseToMavenCentral"))
}
