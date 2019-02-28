package knote.tornadofx

import javafx.application.Application
import knote.KNote
import knote.api.PageManager
import knote.tornadofx.model.PageModel
import knote.tornadofx.model.PageRegistryScope
import knote.tornadofx.view.Workbench
import mu.KLogging
import tornadofx.*
import java.io.BufferedReader

class ViewerApp : App(Workspace::class) {
    lateinit var pageManager: PageManager
    private val pages: ArrayList<PageModel> = arrayListOf()

    init {
        KNote.NOTEBOOK_MANAGER.evalNotebooks()
        val notebooks = KNote.NOTEBOOK_MANAGER.notebooks

        // TODO() make sure every workspace is one notebook
        notebooks.forEach { (id, notebook) ->
            logger.info("id: $id")
            pageManager = notebook.pageManager!!
//            pageManager.executeAll()
            val pages = pageManager.pages
            pages.forEach { (pageId, page) ->
                val result = pageManager.getResultOrExec(pageId)
                logger.info("[$pageId]: ${result?.let { "KClass: ${it::class}" }} value: '$result'")
                convertNotebookScriptToParams(page, result.toString())
            }
        }
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<Workbench>(PageRegistryScope(pageManager, pages))
    }

    private fun convertNotebookScriptToParams(page: knote.api.Page, results: String?) {
        val fileText = page.file.bufferedReader().use(BufferedReader::readText)
        val pageModel = PageModel(page.file, page.id, fileText, results)
        pages.add(pageModel)
    }

    companion object : KLogging() {
        @JvmStatic
        fun main(vararg args: String) {
            KNote.NOTEBOOK_MANAGER.notebookFilter = args.toList()
            Application.launch(ViewerApp::class.java, *args)
        }
    }
}