package knote.api

import knote.util.KObservableObject

interface NotebookManager {
    val notebookObject: KObservableObject<NotebookManager, Notebook>
    val notebook get() = notebookObject.value

    fun compileNotebookCached(): Notebook?
    fun compileNotebook(): Notebook?

    fun getPageManager(): PageManager?
}