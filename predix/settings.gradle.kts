rootProject.name = "predix"

pluginManagement {
    includeBuild("../monorepo-convention-plugins")
}

plugins {
    id("io.github.tacascer.monorepo.settings-convention") version "+"
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
