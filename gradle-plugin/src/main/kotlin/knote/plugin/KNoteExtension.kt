package knote.plugin

import org.gradle.api.Project

open class KNoteExtension(val project: Project) {
    var notebooks: List<NotebookModel> = listOf()
        private set

    fun notebook(id: String, configure: NotebookModel.() -> Unit = {}) {
        val model = NotebookModel(id, project)
        model.configure()
        notebooks += model
    }
}