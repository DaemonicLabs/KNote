package knote.api

import javafx.collections.ObservableMap
import knote.script.NotebookScript
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

interface NotebookRegisty {
    val compiledNotebooks: ObservableMap<String, NotebookScript>
    val reportMap: ObservableMap<String, List<ScriptDiagnostic>>
    /**
     * list of notebook ids to get evaluated
     */
    var notebookFilter: List<String>?

    fun evalNotebooks()
    fun findNotebook(notebookId: String): NotebookScript?
    fun evalNotebook(notebookId: String): NotebookScript?

    @Deprecated("please use compiledNotebooks")
    val notebooks: List<NotebookScript>
        get() = compiledNotebooks.values.toList()


    val listNotebookFiles: Array<out File>
}