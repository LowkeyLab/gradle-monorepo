import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("kotlin-multiplatform-conventions")
    id(
        libs.plugins.kotlinx.kover
            .get()
            .pluginId,
    )
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = rootProject.group
version = rootProject.version

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.google.ksp.symbolProcessingApi)
                implementation(libs.square.kotlinPoet)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name.set("Free-DSL")
        description.set(
            "Kotlin Symbol Processor for generating idiomatic Kotlin DSL builders for data classes and regular classes with primary constructors.",
        )
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

tasks.releaseCI {
    dependsOn(tasks.named("publishAndReleaseToMavenCentral"))
}
