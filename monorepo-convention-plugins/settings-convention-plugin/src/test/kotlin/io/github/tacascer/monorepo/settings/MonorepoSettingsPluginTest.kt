package io.github.tacascer.monorepo.settings

import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainInOrder
import jdk.internal.vm.vector.VectorSupport.test

class MonorepoSettingsPluginTest :
    io.kotest.core.spec.style.FunSpec({
        listOf(
            "lint",
            "check",
            "build",
            "release",
            "lintCI",
            "checkCI",
            "buildCI",
            "releaseCI",
        ).forEach {
            test("when plugin is applied, $it task is created in projects") {
                val testDir = tempdir()
                val settingsFile = testDir.resolve("settings.gradle.kts")
                val buildFile = testDir.resolve("build.gradle.kts")

                settingsFile.writeText(
                    """
                    rootProject.name = "test"
                    
                    plugins {
                        id("io.github.tacascer.monorepo.settings-convention")
                    }
                    """.trimIndent(),
                )

                buildFile.writeText(
                    """
                    plugins {
                        base
                    }
                    """.trimIndent(),
                )

                val result =
                    org.gradle.testkit.runner.GradleRunner
                        .create()
                        .withProjectDir(testDir)
                        .withArguments(it)
                        .withPluginClasspath()
                        .build()

                result.task(":$it")?.outcome shouldBe org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE
            }
        }

        test("when plugin is applied, task report task shows CI and Developer groups") {
            val testDir = tempdir()
            val settingsFile = testDir.resolve("settings.gradle.kts")
            val buildFile = testDir.resolve("build.gradle.kts")

            settingsFile.writeText(
                """
                rootProject.name = "test"
                
                plugins {
                    id("io.github.tacascer.monorepo.settings-convention")
                }
                """.trimIndent(),
            )

            buildFile.writeText(
                """
                plugins {
                    base
                }
                """.trimIndent(),
            )

            val result =
                org.gradle.testkit.runner.GradleRunner
                    .create()
                    .withProjectDir(testDir)
                    .withArguments("tasks")
                    .withPluginClasspath()
                    .build()

            result.output.shouldContainInOrder("CI tasks", "buildCI", "checkCI", "lintCI", "releaseCI")
            result.output.shouldContainInOrder("Developer tasks", "build", "check", "lint", "release")
        }
    })
