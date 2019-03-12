package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import knote.KNote
import knote.tornadofx.Styles
import knote.tornadofx.model.NotebookScope
import knote.tornadofx.model.PageViewModel
import tornadofx.*

class NotebookSpace : View() {

    var pages = arrayListOf<PageViewModel>().observable()
    private val tools = (1..10).toList()

    override val scope = super.scope as NotebookScope

    init {
        scope.pageViewModels.forEach {
            pages.add(it)
        }
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
//                                                it.script = new
                                                val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager()!!
                                                pageManager.updateSourceCode(it.pageId, new)
                                            }
                                            font = Font.font("monospaced", font.size)
                                        }
                                        // TODO() redo results to accept any, check NikkyAi's branch update for that
                                        vbox {
                                            textarea(stringBinding(it.resultProperty) { get().toString() }) {
                                                isEditable = false
                                                font = Font.font("monospaced", font.size)
                                                style {
                                                    hgrow = Priority.ALWAYS
                                                    vgrow = Priority.ALWAYS
                                                }
                                            }
                                            style {
                                                hgrow = Priority.ALWAYS
                                                vgrow = Priority.ALWAYS
                                            }
                                            minHeight = 280.0
                                        }.addClass(Styles.evaluationConsole)

                                        hbox {
                                            pane { hboxConstraints { hGrow = Priority.ALWAYS } }
                                            button("Force Rerun") {
                                                enableWhen(it.dirtyStateProperty)
                                                setOnAction {
                                                    scope.pageManager.executePage(page.pageId)
                                                }
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