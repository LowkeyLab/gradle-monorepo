rootProject.name = "kotlinx-serialization-ber"

pluginManagement {
    includeBuild("../monorepo-convention-plugins")
}

plugins {
    id("io.github.tacascer.monorepo.settings-convention")
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
