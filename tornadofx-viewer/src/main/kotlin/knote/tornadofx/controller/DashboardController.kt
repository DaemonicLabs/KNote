package knote.tornadofx.controller

import knote.tornadofx.view.Dashboard
import tornadofx.*

class DashboardController: Controller() {

    private val dashboard: Dashboard by inject()
    lateinit var notebookId: String

    fun showWorkbench(notebookId: String) {
        this.notebookId = notebookId
        dashboard.currentWindow?.hide()
        workspace.openWindow()
    }
}