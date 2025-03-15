package io.github.lowkeylab.todograph

class TaskDto(
    override val name: String,
    override val description: String,
    override val dependents: List<Task>,
    id: String? = null,
) : Task {
    override val id: Long? = id?.toLong()
}