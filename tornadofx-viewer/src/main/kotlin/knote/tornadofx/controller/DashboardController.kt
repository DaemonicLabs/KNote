package knote.tornadofx.controller

import knote.tornadofx.view.Dashboard
import tornadofx.*

class DashboardController: Controller() {

    private val dashboard: Dashboard by inject()

    fun showWorkbench() {
        dashboard.currentWindow?.hide()
        workspace.openWindow()
    }
}