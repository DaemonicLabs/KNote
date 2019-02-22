package knote.tornadofx

import javafx.application.Application
import knote.KNote
import knote.tornadofx.view.MainView
import tornadofx.App

class ViewerApp : App() {
    override val primaryView = MainView::class

    init {
        KNote.notebooks.forEach { notebook ->
            val pageRegistry = KNote.pageRegistries[notebook.id]!!
            pageRegistry.allResults.forEach { pageId, result ->
                println("[$pageId]: KClass: ${result::class} value: '$result'")
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            Application.launch(ViewerApp::class.java, *args)
        }
    }
}