plugins {
    id("monorepo-conventions")
    alias(libs.plugins.benManes.versions)
    alias(libs.plugins.diffplug.spotless)
    alias(libs.plugins.littlerobots.versionCatalogUpdate)
}

group = "io.github.tacascer"

spotless {
    kotlin {
        target("*.kts")
        ktlint()
    }
}

tasks.lint {
    dependsOn(tasks.spotlessApply)
    dependsOn(tasks.versionCatalogFormat)
}

val testClasses by tasks.registering {
    description = "Dummy task for CodeQL Autobuild"
}
