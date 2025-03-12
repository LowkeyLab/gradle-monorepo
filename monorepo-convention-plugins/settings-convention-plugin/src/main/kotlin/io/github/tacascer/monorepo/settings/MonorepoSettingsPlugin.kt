package io.github.tacascer.monorepo.settings

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings

class MonorepoSettingsPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        println("Applying $target")
    }
}
