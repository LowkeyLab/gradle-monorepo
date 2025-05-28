pluginManagement {
    includeBuild("monorepo-convention-plugins")
}

plugins {
    id("com.gradle.develocity") version "4.0.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("io.github.tacascer.monorepo.settings-convention")
}

develocity {
    buildScan {
        termsOfUseUrl = "https://gradle.com/terms-of-service"
        termsOfUseAgree = "yes"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "gradle-monorepo"

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("pto-scheduler")
includeBuild("predix")
includeBuild("guess-the-word")
includeBuild("kotlinx-serialization-ber")
includeBuild("free-dsl")
