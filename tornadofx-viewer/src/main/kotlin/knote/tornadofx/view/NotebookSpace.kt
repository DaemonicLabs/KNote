package knote.tornadofx.view

import javafx.geometry.Side
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import knote.tornadofx.Styles
import knote.tornadofx.controller.NotebookSpaceController
import knote.tornadofx.model.PageManagerChangeListener
import knote.tornadofx.model.PageManagerEvent
import knote.tornadofx.model.PageManagerScope
import knote.tornadofx.model.PageViewModel
import tornadofx.*

class NotebookSpace: View() {
    var pages = arrayListOf<PageViewModel>().observable()
    // lateinit var currentPage: PageViewModel
    val tools = (1..10).toList()
    var evaluationConsole = VBox()
    var rerunButton = Button()

    override val scope = super.scope as PageManagerScope
    // val controller: NotebookSpaceController by inject()

    init {
        scope.pageViewModels.forEach {
            pages.add(it)
        }

        // scope.pageManagerObject.asProperty
        //        .readOnlyProperty.addListener(ChangeListener { observable,
        //                                                       oldValue,
        //                                                       newValue ->
        //    controller.updateEvaluationConsole(newValue?.pages!![currentPage.pageId]!!.result.toString())
        // })

        subscribe<PageManagerChangeListener> { event ->
            val eval = scope.pageManager.evalPage(event.pageName)
            fire(PageManagerEvent(eval))
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
                                                // currentPage = page
                                                page.file.printWriter().use { out ->
                                                    out.println(page.script)
                                                }
                                                fire(PageManagerChangeListener(page.pageId, scope))
                                                // scope.pageManager.evalPage(page.pageId)
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