package knote.tornadofx

import javafx.application.Application
import knote.KNote
import knote.api.PageManager
import knote.poet.NotePage
import knote.tornadofx.model.Page
import knote.tornadofx.model.PageRegistryScope
import knote.tornadofx.view.Workbench
import mu.KLogging
import tornadofx.*
import java.io.BufferedReader

class ViewerApp : App(Workspace:: class) {
    lateinit var pageManager: PageManager
    private val pages: ArrayList<Page> = arrayListOf()

    init {
        KNote.NOTEBOOK_REGISTRY.evalNotebooks()
        val notebooks = KNote.NOTEBOOK_REGISTRY.notebooks

        // TODO() make sure every workspace is one notebook
        notebooks.forEach { notebook ->
            pageManager = KNote.PAGE_REGISTRIES.getValue(notebook.id)
            pageManager.allResults.forEach { pageId, result ->
                logger.info("[$pageId]: KClass: ${result::class} value: '$result'")
            }
            notebook.includes.forEach{ notePage ->
                val result = pageManager.getResultOrExec(notePage.id)
                convertNotebookScriptToParams(notePage, result.toString())
            }
        }
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<Workbench>(PageRegistryScope(pageManager, pages))
    }

    private fun convertNotebookScriptToParams(notePage: NotePage, results: String?)  {
        val fileText = notePage.file.bufferedReader().use(BufferedReader::readText)
        val page = Page(notePage.file, notePage.id, fileText, results)
        pages.add(page)
    }

    companion object: KLogging() {
        @JvmStatic
        fun main(vararg args: String) {
            KNote.NOTEBOOK_REGISTRY.notebookFilter = args.toList()
            Application.launch(ViewerApp::class.java, *args)
        }
    }
}