package knote.tornadofx.view

import javafx.collections.ObservableList
import knote.KNote
import knote.tornadofx.model.NotebookModel
import knote.tornadofx.model.NotebookScope
import knote.tornadofx.model.PageViewModel
import mu.KotlinLogging
import tornadofx.*

class NotebookWorkbench : Workspace() {
    val logger = KotlinLogging.logger {}
    override fun onBeforeShow() {
        KNote.evalNotebook()
        logger.info("id: ${KNote.notebookId}")
        val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager()
            ?: throw IllegalStateException("cannot load page manager for ${KNote.notebookId}")

        val pageViewModels: ObservableList<PageViewModel> = observableList()
        val pages = pageManager.pages

        pages.forEach { (pageId, page) ->
            val result = pageManager.executePageCached(pageId)
            logger.info("[$pageId]: ${result?.let { "KClass: ${it::class}" }} value: '$result'")
            pageViewModels += PageViewModel(
                page
            )
        }

        val notebookModel = NotebookModel(KNote.NOTEBOOK_MANAGER.notebook, pageManager, pageViewModels)

        val notebookScope = NotebookScope(
            notebookModel.notebook,
            notebookModel.pageManager,
            notebookModel.pageViewModels
        )

        workspace.dock<NotebookSpace>(notebookScope)
    }
}
