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
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        jvmTest {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter-api:5.9.1")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.1")
            }
        }
    }
}

// Add the annotation processor as a KSP dependency
dependencies {
    add("kspJvm", project(":annotation-processor"))
}
