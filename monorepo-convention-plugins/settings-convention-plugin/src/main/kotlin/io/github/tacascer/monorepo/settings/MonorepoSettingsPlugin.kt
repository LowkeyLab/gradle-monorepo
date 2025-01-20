package io.github.tacascer.monorepo.settings

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.diagnostics.TaskReportTask

const val CI_GROUP_NAME = "CI"

const val DEVELOPER_GROUP_NAME = "Developer"

/**
 * This plugin adds the following tasks for CI:
 * - lintCI task that runs all lint tasks in all subprojects and included builds
 * - checkCI task that runs all check tasks in all subprojects and included builds
 * - buildCI task that runs all build tasks in all subprojects and included builds
 * - releaseCI task that runs all release tasks in all subprojects and included builds
 *
 * It also adds the following tasks for developers:
 * - lint task that runs linters in a project
 * - release task that releases a project
 */
class MonorepoSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.gradle.lifecycle.beforeProject { project ->
            project.pluginManager.apply(BasePlugin::class.java)
            for (task in Tasks.entries) {
                project.tasks.findByName(task.developerName)?.let {
                    configureExistingTask(project, task)
                } ?: createNewTask(project, task)

                createCITask(project, task)
                configureTaskDisplayTask(project)
            }
        }
    }

    private fun configureTaskDisplayTask(project: Project) {
        project.tasks.withType(TaskReportTask::class.java) {
            it.displayGroups = listOf(CI_GROUP_NAME, DEVELOPER_GROUP_NAME)
        }
    }

    private fun createCITask(
        project: Project,
        task: Tasks,
    ) {
        project.tasks.register(task.ciTaskName) { t ->
            t.group = CI_GROUP_NAME
            t.description = "Run ${task.ciTaskName} in this project, all subprojects, and included builds"
            t.dependsOn(task.developerName)
            t.dependsOn(project.subprojects.map { it.tasks.named(task.ciTaskName) })
            t.dependsOn(project.gradle.includedBuilds.map { it.task(":${task.ciTaskName}") })
        }
    }

    private fun createNewTask(
        project: Project,
        task: Tasks,
    ) {
        project.tasks.register(task.developerName) { t ->
            t.group = DEVELOPER_GROUP_NAME
            t.description = task.description
            if (task.dependency != null) {
                t.dependsOn(task.dependency.developerName)
            }
            project.subprojects.forEach { subproject ->
                subproject.tasks.named(task.developerName).configure {
                    t.dependsOn(it)
                }
            }
            project.gradle.includedBuilds.forEach { includedBuild ->
                t.dependsOn(includedBuild.task(":${task.developerName}"))
            }
        }
    }

    private fun configureExistingTask(
        project: Project,
        task: Tasks,
    ) {
        project.tasks.named(task.developerName).configure { existingTask ->
            existingTask.group = DEVELOPER_GROUP_NAME
            existingTask.description = task.description
            project.subprojects.forEach { subproject ->
                subproject.tasks.named(task.developerName).configure {
                    existingTask.dependsOn(it)
                }
            }
            project.gradle.includedBuilds.forEach { includedBuild ->
                existingTask.dependsOn(includedBuild.task(":${task.developerName}"))
            }
        }
    }
}

private enum class Tasks(
    val developerName: String,
    val description: String,
    val dependency: Tasks?,
) {
    LINT("lint", "Run linters in this project and all included builds", null),
    CHECK("check", "Run tests and integration tests in this project and all included builds", LINT),
    BUILD("build", "Run tests and assembles this project artifacts and all included builds", CHECK),
    RELEASE("release", "Release this project and all included builds", null),
    ;

    val ciTaskName = "${developerName}CI"
}
