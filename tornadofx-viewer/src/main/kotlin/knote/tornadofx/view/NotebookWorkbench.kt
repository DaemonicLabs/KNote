package knote.tornadofx.view

import knote.tornadofx.controller.DashboardController
import knote.tornadofx.model.NotebookScope
import knote.tornadofx.model.NotebookModel
import knote.tornadofx.model.NotebookViewModel
import tornadofx.*

class NotebookWorkbench : Workspace() {
    private val dashboard: Dashboard by inject()
    private val dashboardController: DashboardController by inject()
    private val notebookViewModel: NotebookViewModel by inject()

    val notebooks = dashboard.notebookModels

    override fun onBeforeShow() {
        val currentNotebook = notebooks.filter {
            it.notebook.id == dashboardController.notebookId
        }

        val notebookModel = NotebookModel(currentNotebook[0].notebook,
                currentNotebook[0].pageManager,
                currentNotebook[0].pageViewModels
        )

        workspace.dockInNewScope<NotebookSpace>(NotebookViewModel(notebookModel))
    }
}
