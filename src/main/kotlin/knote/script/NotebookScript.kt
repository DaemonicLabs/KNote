package knote.script

import knote.poet.NotePage
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "NoteBook",
    fileExtension = "notebook.kts",
    compilationConfiguration = NotebookConfiguration::class
)
open class NotebookScript(val id: String) {
    override fun toString() = "NotebookScript(id=$id)"

    var title: String = ""
    var description: String = ""

    val includes: MutableList<NotePage> = mutableListOf()

    fun include(vararg pages: NotePage) {
        includes.addAll(pages)
    }

    // TODO: add notebook title, descriptions, meta info
}