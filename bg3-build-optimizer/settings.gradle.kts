rootProject.name = "bg3-build-optimizer"

pluginManagement {
    includeBuild("../monorepo-convention-plugins")
}

plugins {
    id("io.github.tacascer.monorepo.settings-convention")
}
