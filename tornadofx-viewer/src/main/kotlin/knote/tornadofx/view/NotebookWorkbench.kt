package knote.tornadofx.view

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import knote.KNote
import knote.tornadofx.model.NotebookModel
import knote.tornadofx.model.NotebookScope
import knote.tornadofx.model.PageViewModel
import knote.util.BindingUtil
import knote.util.asObservable
import mu.KotlinLogging
import tornadofx.*

class NotebookWorkbench : Workspace() {
    val logger = KotlinLogging.logger {}
    override fun onBeforeShow() {
        KNote.NOTEBOOK_MANAGER.compileNotebook()
        logger.info("id: ${KNote.notebookId}")
        val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager()
            ?: throw IllegalStateException("cannot load page manager for ${KNote.notebookId}")

        val pageViewModels: ObservableList<PageViewModel> = observableList()

        BindingUtil.mapContent(pageViewModels, pageManager.pages.asObservable) { pageId, page ->
            val result = pageManager.executePageCached(pageId)
            logger.info("[$pageId]: ${result?.let { "KClass: ${it::class}" }} value: '$result'")
            PageViewModel(
                page
            ).also {
                logger.debug("mapped '$pageId'")
            }
        }

        pageViewModels.addListener(ListChangeListener { change ->
            while(change.next()) {
                if(change.wasAdded()) {
                    logger.info("added: ${change.addedSubList}")
                }
                if(change.wasRemoved()) {
                    logger.info("removed: ${change.removed}")
                }
            }
        })

        val notebookModel = NotebookModel(KNote.NOTEBOOK_MANAGER.notebook, pageManager, pageViewModels)

        val notebookScope = NotebookScope(
            notebookModel.notebook,
            notebookModel.pageManager,
            notebookModel.pageViewModels
        )

        workspace.dock<NotebookSpace>(notebookScope)
    }
}
