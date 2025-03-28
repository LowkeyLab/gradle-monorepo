plugins {
    id("kotlin-multiplatform-conventions")
    alias(libs.plugins.google.ksp)
}

group = rootProject.group
version = rootProject.version

kotlin {
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(project(":annotation-processor"))
            }
            kotlin.srcDir("${project.projectDir}/src/generated/kotlin")
        }
        commonTest {
            dependencies {
                implementation(libs.kotest.assertions.core)
            }
        }
    }
}

// Add the annotation processor as a KSP dependency
dependencies {
    add("kspJvm", project(":annotation-processor"))
}

// Configure KSP to output generated code to a git-trackable directory
ksp {
    // Use the absolute path to ensure KSP can find the directory
    arg("ksp.kotlin.generated", file("${project.projectDir}/src/generated/kotlin").absolutePath)
}
