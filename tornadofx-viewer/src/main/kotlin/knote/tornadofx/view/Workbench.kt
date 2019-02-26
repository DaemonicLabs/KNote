package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import knote.tornadofx.Styles
import knote.tornadofx.controller.WorkbenchController

import knote.tornadofx.model.Page
import knote.tornadofx.model.PageRegistryScope
import tornadofx.*

class Workbench : View() {

    var pages = arrayListOf<Page>().observable()
    val tools = (1..10).toList()
    var evaluationConsole = VBox()

    override val scope = super.scope as PageRegistryScope
    private val controller: WorkbenchController by inject()

    init {
        scope.pages.forEach {
            pages.add(it)
        }
    }

    override val root = tabpane {
        pages.forEach { page ->
            tab(page.pageName) {
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
                                                is String -> { add(text(it.results)) }
                                                else -> TODO()
                                            }
                                            minHeight = 280.0
                                        }.addClass(Styles.evaluationConsole)
                                    }
                                }
                            }
                            hbox {
                                pane { hboxConstraints { hGrow = Priority.ALWAYS } }
                                button("Rerun") {
                                    setOnAction {
                                        pages.forEach {page ->
                                            if (page.dirtyState) {
                                                page.file.printWriter().use {
                                                    out -> out.println(page.script)
                                                }
                                            }
                                        }
                                        runAsync {
                                            scope.manager.getResultOrExec(page.pageName).toString()
                                        } ui {
                                            controller.updateEvaluationConsole(it)
                                            page.results = it
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
                                item("Page Dependencies") {
                                    text("List of dependencies here")
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
