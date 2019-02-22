package knote.tornadofx

import javafx.application.Application
import knote.KNote
import knote.tornadofx.view.MainView
import knote.tornadofx.view.Workbench
import tornadofx.*

class ViewerApp : App(Workspace:: class) {

    init {
        KNote.notebooks.forEach { notebook ->
            val pageRegistry = KNote.pageRegistries.getValue(notebook.id)
            pageRegistry.allResults.forEach { pageId, result ->
                println("[$pageId]: KClass: ${result::class} value: '$result'")
            }
        }
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<Workbench>()
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            KNote.notebookFilter = args.toList()
            KNote.evalNotebooks()
            Application.launch(ViewerApp::class.java, *args)
        }
    }
}