package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import knote.PageManagerImpl
import knote.api.PageManager
import knote.tornadofx.Styles
import knote.tornadofx.controller.NotebookSpaceController
import knote.tornadofx.model.PageManagerChangeListener
import knote.tornadofx.model.PageManagerEvent
import knote.tornadofx.model.PageManagerScope
import knote.tornadofx.model.PageViewModel
import tornadofx.*

class NotebookSpace: View(), PageManagerImpl.PageListener {

    var pages = arrayListOf<PageViewModel>().observable()
    private val tools = (1..10).toList()
    var evaluationConsole = VBox()
    var rerunButton = Button()

    override val scope = super.scope as PageManagerScope
    private val controller: NotebookSpaceController by inject()

    init {
        scope.pageViewModels.forEach {
            pages.add(it)
        }

        scope.pageManager.setPageListener(this)
    }

    override fun onResultsUpdated(result: Any?) {
        controller.updateEvaluationConsole(result.toString())
    }

    override val root = tabpane {
        pages.forEach { page ->
            tab(page.pageId) {
                borderpane {
                    center {
                        vbox {
                            children.bind(pages) {
                                vbox {
                                    if (it == page) {
                                        textarea(it.script) {
                                            textProperty().addListener { _, _, new ->
                                                it.dirtyState = true
                                                it.script = new
                                            }
                                        }
                                        evaluationConsole = vbox {
                                            when (it.results) {
                                                is String -> {
                                                    add(text(it.results))
                                                }
                                                else -> TODO()
                                            }
                                            minHeight = 280.0
                                        }.addClass(Styles.evaluationConsole)
                                    }
                                }
                            }
                            hbox {
                                pane { hboxConstraints { hGrow = Priority.ALWAYS } }
                                rerunButton = button("Rerun") {
                                    setOnAction {
                                        rerunButton.isDisable = true
                                        pages.forEach { page ->
                                            if (page.dirtyState) {
                                                page.file.printWriter().use { out ->
                                                    out.println(page.script)
                                                }

                                                scope.pageManager.evalPage(page.pageId)
                                                page.dirtyState = false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    right {
                        vbox {
                            maxWidth = 300.0
                            drawer(side = Side.RIGHT) {
                                item("Tools", expanded = true) {
                                    datagrid(tools) {
                                        maxCellsInRow = 2
                                        cellWidth = 100.0
                                        cellHeight = 100.0

                                        paddingTop = 15.0
                                        paddingLeft = 35.0
                                        minWidth = 300.0

                                        cellCache {
                                            stackpane {
                                                circle(radius = 25.0) {
                                                    fill = Color.FORESTGREEN
                                                }
                                                label(it.toString())
                                            }
                                        }
                                    }
                                }
                                item("Notebooks") {
                                    text("notebooks")
                                }
                                item("JVM Dependencies") {
                                    text("List of JVM dependencies here")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}