plugins {
    id("kotlin-conventions")
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    js {
        compilerOptions {
            target = "es2015"
        }
        generateTypeScriptDefinitions()
        browser()
        binaries.executable()
    }
}
