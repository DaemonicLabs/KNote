package knote.tornadofx

import knote.tornadofx.view.NotebookWorkbench
import tornadofx.*

class ViewerApp : App(NotebookWorkbench::class) {
    init {
//        FX.defaultWorkspace = NotebookWorkbench::class
//        workspace.openWindow()
    }
}