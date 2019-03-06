package knote.api

import knote.script.NotebookScript
import knote.util.KObservableMap
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

interface NotebookManager {
    val notebooks: KObservableMap<String, Notebook>

    @Deprecated("use notebooks::compiledScript")
    val compiledNotebooks: Map<String, NotebookScript>
        get() {
            return notebooks
                .mapValues { it.value.compiledScript }
                .filterValues { it != null }
                .mapValues { it.value!! }
        }
    @Deprecated("use notebooks::reports")
    val reportMap: Map<String, List<ScriptDiagnostic>>
        get() {
            return notebooks
                .mapValues { it.value.reports }
                .filterValues { it != null }
                .mapValues { it.value!! }
        }
    /**
     * list of notebook ids to get evaluated
     */
    var notebookFilter: List<String>?

    fun evalNotebooks()
    fun compileNotebookCached(notebookId: String): Notebook?
    fun compileNotebook(notebookId: String): Notebook?

    val listNotebookFiles: Array<out File>
    fun listAvailable(): List<String>
    fun getPageManager(notebookId: String): PageManager?
}