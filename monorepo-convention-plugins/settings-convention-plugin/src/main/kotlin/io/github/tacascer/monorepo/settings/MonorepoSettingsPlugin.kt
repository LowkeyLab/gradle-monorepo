package io.github.tacascer.monorepo.settings

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.diagnostics.TaskReportTask

const val CI_GROUP_NAME = "CI"
const val DEVELOPER_GROUP_NAME = "Developer"

class MonorepoSettingsPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        settings.gradle.lifecycle.beforeProject { project ->
            project.pluginManager.apply(BasePlugin::class.java)
            Tasks.entries.forEach { task ->
                configureTask(project, task)
                configureTaskDisplayTask(project)
            }
        }
    }

    private fun configureTaskDisplayTask(project: Project) {
        project.tasks.withType(TaskReportTask::class.java) {
            it.displayGroups = listOf(CI_GROUP_NAME, DEVELOPER_GROUP_NAME)
        }
    }

    private fun configureTask(
        project: Project,
        task: Tasks,
    ) {
        // Configure or create developer task
        project.tasks.findByName(task.developerName)?.let {
            project.tasks.named(task.developerName).configure { t ->
                configureDeveloperTask(project, t, task)
            }
        } ?: project.tasks.register(task.developerName) { t ->
            configureDeveloperTask(project, t, task)
        }

        // Create CI task
        project.tasks.register(task.ciTaskName) { t ->
            t.group = CI_GROUP_NAME
            t.description =
                "Run ${task.ciTaskName} in this project, all subprojects, and included builds"
            t.dependsOn(task.developerName)
            t.dependsOn(project.subprojects.map { "${it.path}:${task.ciTaskName}" })
            t.dependsOn(project.gradle.includedBuilds.map { it.task(":${task.ciTaskName}") })
        }
    }

    private fun configureDeveloperTask(
        project: Project,
        task: org.gradle.api.Task,
        taskType: Tasks,
    ) {
        task.group = DEVELOPER_GROUP_NAME
        task.description = taskType.description

        // Add dependency on previous task if defined
        taskType.dependency?.let { dependency ->
            task.dependsOn(dependency.developerName)
        }

        // Add dependencies on same task in subprojects
        task.dependsOn(project.subprojects.map { "${it.path}:${taskType.developerName}" })

        // Add dependencies on same task in included builds
        task.dependsOn(project.gradle.includedBuilds.map { it.task(":${taskType.developerName}") })
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
