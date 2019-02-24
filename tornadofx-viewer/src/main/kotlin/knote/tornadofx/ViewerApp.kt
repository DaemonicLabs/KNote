package knote.tornadofx

import javafx.application.Application
import knote.KNote
import knote.api.NotebookRegisty
import knote.api.PageRegistry
import knote.poet.NotePage
import knote.poet.PageMarker
import knote.tornadofx.model.Page
import knote.tornadofx.model.PageRegistryScope
import knote.tornadofx.view.Workbench
import tornadofx.*
import java.io.BufferedReader
import java.io.File

class ViewerApp : App(Workspace:: class) {

    lateinit var pageRegistry: PageRegistry
    private val pages: ArrayList<Page> = arrayListOf()

    init {
        KNote.notebookRegistry.evalNotebooks()
        val notebooks = KNote.notebookRegistry.notebooks
        // TODO() make sure every workspace is one notebook
        notebooks.forEach { notebook ->
            pageRegistry = KNote.pageRegistries.getValue(notebook.id)
            pageRegistry.allResults.forEach { pageId, result ->
                println("[$pageId]: KClass: ${result::class} value: '$result'")
            }
            notebook.includes.forEach{ notePage ->
                val result = pageRegistry.getResultOrExec(notePage.id)
                convertNotebookScriptToParams(notePage, result.toString())
            }
        }
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<Workbench>(PageRegistryScope(pageRegistry, pages))
    }

    private fun convertNotebookScriptToParams(notePage: NotePage, results: String?)  {
        val fileText = notePage.file.bufferedReader().use(BufferedReader::readText)
        val page = Page(notePage.file, notePage.id, fileText, results)
        pages.add(page)
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            KNote.notebookRegistry.notebookFilter = args.toList()
            Application.launch(ViewerApp::class.java, *args)
        }
    }
}