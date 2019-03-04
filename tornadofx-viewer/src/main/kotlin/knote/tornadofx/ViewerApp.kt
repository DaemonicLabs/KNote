package knote.tornadofx

import javafx.application.Application
import javafx.beans.property.ReadOnlyMapProperty
import javafx.beans.property.ReadOnlyObjectWrapper
import knote.KNote
import knote.api.Notebook
import knote.api.PageManager
import knote.tornadofx.model.PageViewModel
import knote.tornadofx.model.PageManagerScope
import knote.tornadofx.view.Workbench
import knote.util.KObservableObject
import knote.util.asProperty
import mu.KLogging
import tornadofx.*

class ViewerApp : App(Workspace::class) {
    lateinit var pageManager: PageManager
    lateinit var pageManagerObject: KObservableObject<Notebook, PageManager?>
    private val pageViewModels: ArrayList<PageViewModel> = arrayListOf()

    init {
        KNote.NOTEBOOK_MANAGER.evalNotebooks()
        val notebooks = KNote.NOTEBOOK_MANAGER.notebooks

        // TODO include a mechanism to choose a notebook, but we'll make the first notebook default for now
        notebooks.forEach { (id, notebook) ->
            logger.info("id: $notebook.id")
            pageManager = notebook.pageManager!!
            pageManagerObject = notebook.pageManagerObject

            val pages = pageManager.pages

            pages.forEach { (pageId, page) ->
                val result = pageManager.getResultOrExec(pageId)
                logger.info("[$pageId]: ${result?.let { "KClass: ${it::class}" }} value: '$result'")
                pageViewModels.add(PageViewModel(
                                page.file,
                                page.id,
                                page.fileContent,
                                page.result?.toString() ?: ""
                        ))
            }
        }
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<Workbench>(PageManagerScope(pageManager, pageManagerObject, pageViewModels))
    }

    companion object : KLogging() {
        @JvmStatic
        fun main(vararg args: String) {
            KNote.NOTEBOOK_MANAGER.notebookFilter = args.toList()
            Application.launch(ViewerApp::class.java, *args)
        }
    }
}