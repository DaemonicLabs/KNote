package knote.api

import knote.script.NotebookScript
import java.io.File

interface NotebookRegisty {

    /**
     * list of notebook ids to get evaluated
     */
    var notebookFilter: List<String>?

    fun evalNotebooks()
    fun findNotebook(notebookId: String): NotebookScript?
    fun evalNotebook(notebookId: String): NotebookScript?

    /**
     * Evaluate all notebooks (filters apply)
     */
    val notebooks: List<NotebookScript>
    val notebookFiles: Array<out File>
}