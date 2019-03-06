package knote.tornadofx.view

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.text.Font
import knote.KNote
import knote.api.PageManager
import knote.tornadofx.ViewerApp
import knote.tornadofx.controller.DashboardController
import knote.tornadofx.model.NotebookModel
import knote.tornadofx.model.PageViewModel
import mu.KLogging
import tornadofx.*

class Dashboard: View() {

    lateinit var pageManager: PageManager
    val notebookModels: ArrayList<NotebookModel> = arrayListOf()
    private val notebookList: ArrayList<String> = arrayListOf()
    private val controller: DashboardController by inject()

    init {
        KNote.NOTEBOOK_MANAGER.evalNotebooks()
        val notebooks = KNote.NOTEBOOK_MANAGER.notebooks

        // TODO include a mechanism to choose a notebook, but we'll make the first notebook default for now
        notebooks.forEach { (id, notebook) ->
            notebookList.add(notebook.id)
            logger.info("id: $notebook.id")
            pageManager = notebook.pageManager!!

            val pageViewModels: ArrayList<PageViewModel> = arrayListOf()
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
            notebookModels.add(NotebookModel(notebook, pageManager, pageViewModels.observable()))
        }
    }

    private val paginator = DataGridPaginator(notebookList.observable(), itemsPerPage = 4)

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
                    controller.showWorkbench(it.toString())
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