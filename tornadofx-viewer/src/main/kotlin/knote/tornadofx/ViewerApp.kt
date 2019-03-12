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
    var pageManager: PageManager
    private val pages: ArrayList<PageModel> = arrayListOf()

    init {
        logger.info("id: ${KNote.notebookId}")
        pageManager = KNote.NOTEBOOK_MANAGER.getPageManager()
            ?: throw IllegalStateException("cannot load page manager for ${KNote.notebookId}")
//            pageManager.executeAll()
        val pages = pageManager.pages
        pages.forEach { (pageId, page) ->
            val result = pageManager.executePageCached(pageId)
            logger.info("[$pageId]: ${result?.let { "KClass: ${it::class}" }} value: '$result'")
            convertNotebookScriptToParams(page, result.toString())
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
            Application.launch(ViewerApp::class.java, *args)
        }
    }
}