package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Text

import knote.tornadofx.model.Page
import knote.tornadofx.model.PageRegistryScope
import tornadofx.*

class Workbench : View() {

    var pages = arrayListOf<Page>().observable()
    val tools = (1..10).toList()
    var evaluationText = Text()
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
                                        vbox {
                                            when (it.results) {
                                                is String -> {
                                                    evaluationText = text(it.results)
                                                    add(evaluationText)
                                                }
                                                else -> TODO()
                                            }
                                            minHeight = 280.0
                                            style {
                                                backgroundColor += Color.WHITE
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
<<<<<<< HEAD
                                    setOnAction {
                                        pages.forEach {page ->
                                            if (page.dirtyState) {
                                                page.file.printWriter().use {
                                                    out -> out.println(page.script)
                                                }
                                            }
                                        }
                                        page.results = scope.registry.execPage(page.pageName).toString()
                                        evaluationText = text(page.results)
                                    }
=======
                                    setOnAction { KNote.evalNotebook(page.file) }
>>>>>>> eb19cba63766636ff2ffff5ff4dcc17f0cb57a04
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
