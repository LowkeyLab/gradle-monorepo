import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-gradle-plugin`
    `jvm-test-suite`
    alias(libs.plugins.gradle.pluginPublish)
    alias(libs.plugins.gradleup.shadow)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.kover)
}

group = "io.github.tacascer"
version = rootProject.version

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(libs.kotest.runnerJunit5)
            }
        }
    }
}

gradlePlugin {
    plugins {
        create("monorepoConventionPlugin") {
            id = "io.github.tacascer.monorepo.settings-convention"
            implementationClass = "io.github.tacascer.monorepo.settings.MonorepoSettingsPlugin"
        }
    }
}

dependencies {
    shadow(localGroovy())
    shadow(gradleApi())
}

tasks.named("shadowJar", ShadowJar::class) {
    isEnableRelocation = true
    archiveClassifier = ""
}

tasks.jar {
    archiveClassifier = "plain"
}
