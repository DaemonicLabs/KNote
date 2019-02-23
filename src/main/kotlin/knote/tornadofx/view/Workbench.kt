package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.paint.Color
import knote.tornadofx.ViewerApp
import knote.tornadofx.model.Page
import knote.tornadofx.model.PageModel
import tornadofx.*

class Workbench : View() {

    val pageModel: PageModel by inject()
    var pages = arrayListOf<Page>().observable()
    val tools = (1..10).toList()

    init {
        params.entries.forEach {
            val page = Page(it.key, params[it.key] as String)
            pages.add(page)
        }
    }

    override val root = tabpane {
        pages.forEach {
            tab(it.name) {
                borderpane {
                    center = stackpane {
                        textarea(it.script)
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
                            }
                        }
                    }
                    bottom {
                        textarea("Compiled results here")
                    }
                }
            }
        }
    }
}
