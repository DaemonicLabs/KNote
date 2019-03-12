package knote.plugin

import org.gradle.api.Project

class NotebookModel(val id: String, project: Project) {
    var notebookDir = project.rootDir.resolve(id)
    var mainFile: String = "$id.notebook.kts"

    val dependencies = KNoteDependencyScope(id, project)
    fun dependencies(configuration: KNoteDependencyScope.() -> Unit) =
        dependencies.configuration()
}