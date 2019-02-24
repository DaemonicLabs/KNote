package knote.tornadofx

import javafx.application.Application
import knote.KNote
import knote.poet.NotePage
import knote.tornadofx.model.Page
import knote.tornadofx.view.Workbench
import tornadofx.*
import java.io.BufferedReader

class ViewerApp : App(Workspace:: class) {

    private val pages: MutableMap<String, Page> = mutableMapOf()

    init {
        // evaluate
        KNote.notebooks.forEach { notebook ->
            val pageRegistry = KNote.pageRegistries.getValue(notebook.id)
            pageRegistry.allResults.forEach { pageId, result ->
                println("[$pageId]: KClass: ${result::class} value: '$result'")
            }
            notebook.includes.forEach{ notePage ->
                val result = pageRegistry.result[notePage.id]
                convertNotebookScriptToParams(notePage, result.toString())
            }
        }
    }

    override fun onBeforeShow(view: UIComponent) {
        workspace.dock<Workbench>(params = pages)
    }

    private fun convertNotebookScriptToParams(notePage: NotePage, results: String?) {
        val fileText = notePage.file.bufferedReader().use(BufferedReader::readText)
        val page = Page(notePage.id, fileText, results)
        pages["${notePage.id}.page.kts"] = page
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