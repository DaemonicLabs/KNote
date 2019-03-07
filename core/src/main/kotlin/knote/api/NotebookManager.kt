package knote.api

import knote.script.NotebookScript
import knote.util.KObservableMap
import knote.util.KObservableObject
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

interface NotebookManager {
    val notebookObject: KObservableObject<NotebookManager, Notebook?>
    val notebook get() = notebookObject.value

    fun compileNotebookCached(): Notebook?
    fun compileNotebook(): Notebook?

    fun getPageManager(): PageManager?
}