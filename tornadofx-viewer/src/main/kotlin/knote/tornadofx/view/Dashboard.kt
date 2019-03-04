package knote.tornadofx.view

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.text.Font
import knote.KNote
import knote.api.Notebook
import knote.api.PageManager
import knote.tornadofx.ViewerApp
import knote.tornadofx.controller.DashboardController
import knote.tornadofx.controller.NotebookSpaceController
import knote.tornadofx.model.PageManagerScope
import knote.tornadofx.model.PageViewModel
import knote.util.KObservableObject
import mu.KLogging
import tornadofx.*

class Dashboard: View() {

    lateinit var pageManager: PageManager
    lateinit var pageManagerObject: KObservableObject<Notebook, PageManager?>
    val pageViewModels: ArrayList<PageViewModel> = arrayListOf()
    val controller: DashboardController by inject()

    init {
        FX.defaultWorkspace = NotebookWorkbench::class

        KNote.NOTEBOOK_MANAGER.evalNotebooks()
        val notebooks = KNote.NOTEBOOK_MANAGER.notebooks

        // TODO include a mechanism to choose a notebook, but we'll make the first notebook default for now
        notebooks.forEach { (id, notebook) ->
            logger.info("id: $notebook.id")
            pageManager = notebook.pageManager!!
            pageManagerObject = notebook.pageManagerObject

            val pages = pageManager.pages

            pages.forEach { (pageId, page) ->
                val result = pageManager.getResultOrExec(pageId)
                logger.info("[$pageId]: ${result?.let { "KClass: ${it::class}" }} value: '$result'")
                pageViewModels.add(PageViewModel(
                        page.file,
                        page.id,
                        page.fileContent,
                        page.result?.toString() ?: ""
                ))
            }
        }
    }

    // TODO() get configs to get list of notebooks

    val notebooklist = listOf("NotebookSpace 1", "NotebookSpace 2", "NotebookSpace 3", "Notebook4").observable()

    private val paginator = DataGridPaginator(notebooklist, itemsPerPage = 4)

    override val root = borderpane {
        setPrefSize(450.0, 550.0)
        top {
            label(title) {
                font = Font.font(22.0)
            }
            menubar {
                menu("File") {
                    item("Quit").action {
                        Platform.exit()
                    }
                }
            }
        }

        center {
            datagrid(paginator.items) {
                maxCellsInRow = 2

                cellWidth = 180.0
                cellHeight = 180.0
                paddingLeft = 30.0
                paddingTop = 40.0

                cellCache {
                    stackpane {
                        label(it.toString())
                    }
                }
                onUserSelect(2) {
                    controller.showWorkbench()
                }
            }
        }

        bottom {
            stackpane {
                add(paginator)
                paddingBottom = 25.0
            }
        }
    }

    companion object : KLogging() {
        @JvmStatic
        fun main(vararg args: String) {
            KNote.NOTEBOOK_MANAGER.notebookFilter = args.toList()
            Application.launch(ViewerApp::class.java, *args)
        }
    }
}