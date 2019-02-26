package knote.api

import knote.script.NotebookScript
import knote.util.KObservableMap
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

interface NotebookManager {
    val compiledNotebooks: KObservableMap<String, NotebookScript>
    val reportMap: KObservableMap<String, List<ScriptDiagnostic>>
    /**
     * list of notebook ids to get evaluated
     */
    var notebookFilter: List<String>?

    fun evalNotebooks()
    fun findNotebook(notebookId: String): NotebookScript?
    fun evalNotebook(notebookId: String): NotebookScript?

    @Deprecated("please use compiledNotebooks", ReplaceWith("compiledNotebooks.values.toList()"))
    val notebooks: List<NotebookScript>
        get() = compiledNotebooks.values.toList()


    val listNotebookFiles: Array<out File>
}