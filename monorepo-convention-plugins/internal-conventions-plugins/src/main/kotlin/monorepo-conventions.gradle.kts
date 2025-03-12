plugins {
    base
}

val ciGroupName = "CI"
val developerGroupName = "Developer"

val lint by tasks.registering {
    group = developerGroupName
    description = "Run linters in this project"
    dependOnSubprojectsAndIncludedBuilds(this, "lint")
}

tasks.named("check") {
    group = developerGroupName
    dependsOn(lint)
    dependOnSubprojectsAndIncludedBuilds(this, "check")
}

tasks.named("build") {
    group = developerGroupName
    dependOnSubprojectsAndIncludedBuilds(this, "build")
}

tasks.register("release") {
    group = developerGroupName
    description = "Release this project"
    dependOnSubprojectsAndIncludedBuilds(this, "release")
}

listOf("check", "build", "lint", "release").forEach { taskName ->
    tasks.register("${taskName}CI") {
        group = ciGroupName
        description = "Run $taskName in this project, all subprojects, and included builds"
        dependsOn(tasks.named(taskName))
        dependOnSubprojectsAndIncludedBuilds(this, "${taskName}CI")
    }
}

fun dependOnSubprojectsAndIncludedBuilds(
    task: Task,
    taskName: String,
) {
    task.apply {
        dependsOn(subprojects.map { "${it.name}:$taskName" })
        dependsOn(gradle.includedBuilds.map { it.task(":$taskName") })
    }
}
