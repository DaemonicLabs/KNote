package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import knote.KNote
import knote.tornadofx.model.Page
import knote.tornadofx.model.PageViewModel
import tornadofx.*

class Workbench : View() {

    var pages = arrayListOf<Page>().observable()
    val pageModel: PageViewModel by inject()
    val tools = (1..10).toList()

    init {
        params.entries.forEach {
            pages.add(params[it.key] as Page)
        }
    }

    override val root = tabpane {
        pages.forEach { page ->
            tab(page.pageName) {
                borderpane {
                    center {
                        vbox {
                            hbox {
                                pane { hboxConstraints { hGrow = Priority.ALWAYS } }
                                button("Rerun") {
                                    // setOnAction { KNote.sendToKNote }
                                }
                            }
                            vbox {
                                textarea(page.script)
                                vbox {
                                    when (page.results) {
                                        is String -> add(text {
                                            page.results
                                            // textProperty().bind(pageModel.results)
                                        })
                                        else -> TODO()
                                    }
                                    minHeight = 280.0
                                    style {
                                        backgroundColor += Color.WHITE
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
