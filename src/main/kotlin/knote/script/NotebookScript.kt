package knote.script

import knote.poet.NotePage
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "NoteBook",
    fileExtension = "notebook.kts",
    compilationConfiguration = NotebookConfiguration::class
)
open class NotebookScript(val args: Array<String>) {
    override fun toString() = "NotebookScript(args = ${args.joinToString(" ")})"

    val start = NotePage("_")
    val dependencies: MutableList<Pair<NotePage, NotePage>> = mutableListOf()

    infix fun NotePage.continueWith(nextPage: NotePage): NotePage {
        dependencies += this to nextPage
        return nextPage
    }

    // TODO: add notebook title, descriptions, meta info
}