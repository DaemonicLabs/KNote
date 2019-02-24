package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import knote.KNote
import knote.tornadofx.model.Page
import knote.tornadofx.model.PageRegistryScope
import tornadofx.*

class Workbench : View() {

    var pages = arrayListOf<Page>().observable()
    val tools = (1..10).toList()
    override val scope = super.scope as PageRegistryScope

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
                                        textarea(it.script)
                                        vbox {
                                            when (it.results) {
                                                is String -> add(text(it.results))
                                                else -> TODO()
                                            }
                                            minHeight = 280.0
                                            style {
                                                backgroundColor += Color.WHITE
                                                padding = box(10.px)
                                            }
                                        }
                                    }
                                }
                            }
                            hbox {
                                pane { hboxConstraints { hGrow = Priority.ALWAYS } }
                                button("Rerun") {
                                    setOnAction { scope.registry.execPage(page.pageName) }
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
