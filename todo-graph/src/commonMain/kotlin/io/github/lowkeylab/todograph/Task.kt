package io.github.lowkeylab.todograph

interface Task {
    val name: String
    val description: String
    val dependents: List<Task>
    val id: Long?
}
