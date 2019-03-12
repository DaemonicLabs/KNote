package knote.tornadofx.view

import javafx.collections.ListChangeListener
import javafx.geometry.Side
import javafx.scene.control.TabPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import knote.KNote
import knote.tornadofx.Styles
import knote.tornadofx.model.NotebookScope
import knote.tornadofx.model.PageViewModel
import mu.KotlinLogging
import tornadofx.*

class NotebookSpace : View() {

    val logger = KotlinLogging.logger {}
    private val tools = (1..10).toList()

    override val scope = super.scope as NotebookScope

    fun TabPane.tabPage(addedPage: PageViewModel) {
        logger.info("adding tab for page: ${addedPage.pageId}")
        tab(addedPage.pageId) {
            borderpane {
                center {
                    vbox {
                        vbox {
                            textarea(addedPage.script) {
                                textProperty().addListener { _, _, new ->
                                    addedPage.dirtyState = true
//                                                it.script = new
                                    val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager()!!
                                    pageManager.updateSourceCode(addedPage.pageId, new)
                                }
                                font = Font.font("monospaced", font.size)
                            }
                            // TODO() redo results to accept any, check NikkyAi's branch update for that
                            vbox {
                                textarea(stringBinding(addedPage.resultProperty) { get().toString() }) {
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
                                    enableWhen(addedPage.dirtyStateProperty)
                                    setOnAction {
                                        scope.pageManager.executePage(addedPage.pageId)
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

    override val root = tabpane {
        scope.pageViewModels.addListener(ListChangeListener { change ->
            while(change.next()) {
                logger.debug("change: $change")
                if (change.wasAdded()) {
                    logger.debug("added: ${change.addedSubList}")
                    change.addedSubList.forEach { addedPage ->
                        tabPage(addedPage)
                    }
                }
                if (change.wasRemoved()) {
                    logger.debug("removed: ${change.removed}")
                    change.removed.forEach { removedPage ->
                        val tab = tabs.find { it.text == removedPage.pageId }
                        tabs.remove(tab)
                    }
                }
            }
        })
        scope.pageViewModels.forEach { addedPage ->
            tabPage(addedPage)
        }
    }
}