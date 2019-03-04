package knote.tornadofx.view

import knote.tornadofx.model.PageManagerScope
import tornadofx.*

class NotebookWorkbench : Workspace() {
    val dashboard: Dashboard by inject()

    override fun onBeforeShow() {
        workspace.dock<NotebookWorkbench>(PageManagerScope(
                dashboard.pageManager,
                dashboard.pageManagerObject,
                dashboard.pageViewModels)
        )
    }
}
