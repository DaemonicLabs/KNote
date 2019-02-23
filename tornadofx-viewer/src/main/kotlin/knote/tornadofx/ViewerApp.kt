package knote.tornadofx

import javafx.application.Application
import knote.KNote
import knote.tornadofx.view.MainView
import tornadofx.*

class ViewerApp : App(Workspace:: class) {
    override val primaryView = MainView::class

    init {
        KNote.notebooks.forEach { notebook ->
            val pageRegistry = KNote.pageRegistries.getValue(notebook.id)
            pageRegistry.allResults.forEach { pageId, result ->
                println("[$pageId]: KClass: ${result::class} value: '$result'")
            }
        }
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