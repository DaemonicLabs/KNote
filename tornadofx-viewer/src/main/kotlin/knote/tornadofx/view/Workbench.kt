package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font

import knote.tornadofx.model.PageModel
import knote.tornadofx.model.PageRegistryScope
import tornadofx.*

class Workbench : View() {

    var pages = arrayListOf<PageModel>().observable()
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
                                        textarea(it.script) {
                                            textProperty().addListener { _, _, new ->
                                                it.dirtyState = true
                                                it.script = new
                                            }
                                        }
                                            when (it.results) {
                                                is String -> {
                                                    textarea(it.resultsProperty) {
                                                        isEditable = false
                                                        font = Font.font(java.awt.Font.MONOSPACED, font.size)
                                                        hgrow = Priority.ALWAYS
                                                        vgrow = Priority.ALWAYS
                                                        minHeight = 280.0
                                                    }
                                                }
                                                else -> TODO()
                                            }
                                    }
                                }
                            }
                            hbox {
                                pane { hboxConstraints { hGrow = Priority.ALWAYS } }
                                button("Rerun") {
                                    setOnAction {
                                        pages.forEach { page ->
                                            if (page.dirtyState) {
                                                page.file.printWriter().use { out ->
                                                    out.println(page.script)
                                                }
                                            }
                                        }
                                        page.results = scope.manager.executePage(page.pageName).toString()
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
