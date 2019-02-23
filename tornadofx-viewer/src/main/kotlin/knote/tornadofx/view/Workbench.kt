package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import knote.tornadofx.model.Page
import knote.tornadofx.model.PageModel
import tornadofx.*

class Workbench : View() {

    val pageModel: PageModel by inject()
    var pages = arrayListOf<Page>().observable()
    val tools = (1..10).toList()

    init {
        params.entries.forEach {
            pages.add(params[it.key] as Page)
        }
    }

    override val root = tabpane {
        pages.forEach {page ->
            tab(page.name) {
                borderpane {
                    center {
                        vbox {
                            hbox {
                                pane { hboxConstraints { hGrow = Priority.ALWAYS } }
                                button("Rerun")
                            }
                            vbox {
                                textarea(page.script)
                                vbox {
                                    when (page.results) {
                                        is String -> add(text(page.results as String))
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
