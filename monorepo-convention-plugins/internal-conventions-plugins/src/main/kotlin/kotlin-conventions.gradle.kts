plugins {
    id("com.adarshr.test-logger")
    id("com.diffplug.spotless")
    id("org.jetbrains.kotlinx.kover")
}

spotless {
    kotlin {
        ktlint()
    }
}

tasks.named("lint") {
    dependsOn(tasks.spotlessApply)
}
