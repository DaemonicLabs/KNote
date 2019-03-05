package knote.tornadofx.view

import knote.tornadofx.model.PageManagerScope
import tornadofx.*

class NotebookWorkbench : Workspace() {
    private val dashboard: Dashboard by inject()

    override fun onBeforeShow() {
        workspace.dock<NotebookWorkbench>(PageManagerScope(
                dashboard.pageManager,
                dashboard.pageManagerObject,
                dashboard.pageViewModels)
        )
    }
}
