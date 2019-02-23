package knote.tornadofx

import javafx.application.Application
import knote.KNote
import knote.poet.NotePage
import knote.tornadofx.view.Workbench
import tornadofx.*
import java.io.BufferedReader

class ViewerApp : App(Workspace:: class) {

    private val pages: MutableMap<String, String> = mutableMapOf()

    init {
        // evaluate
        KNote.notebooks.forEach { notebook ->
            notebook.includes.forEach{ notePage -> convertNotebookScriptToParams(notePage) }
            val pageRegistry = KNote.pageRegistries.getValue(notebook.id)
            pageRegistry.allResults.forEach { pageId, result ->
                println("[$pageId]: KClass: ${result::class} value: '$result'")
            }
        }
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<Workbench>(params = pages)
    }

    private fun convertNotebookScriptToParams(notePage: NotePage) {
        val fileText = notePage.file.bufferedReader().use(BufferedReader::readText)
        pages[notePage.id] = fileText
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