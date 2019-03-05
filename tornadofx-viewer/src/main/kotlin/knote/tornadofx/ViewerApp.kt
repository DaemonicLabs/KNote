package knote.tornadofx

import knote.tornadofx.view.Dashboard
import knote.tornadofx.view.NotebookWorkbench
import tornadofx.*

class ViewerApp : App(Dashboard::class) {
    init {
        FX.defaultWorkspace = NotebookWorkbench::class
    }
}