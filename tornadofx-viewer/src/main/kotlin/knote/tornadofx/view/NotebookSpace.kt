package knote.tornadofx.view

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Side
import javafx.scene.control.TabPane
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import kastree.ast.psi.Parser
import knote.KNote
import knote.script.KNConverter
import knote.tornadofx.Styles
import knote.tornadofx.model.NotebookScope
import knote.tornadofx.model.PageViewModel
import mu.KotlinLogging
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.model.StyleSpans
import tornadofx.ChangeListener
import tornadofx.View
import tornadofx.addClass
import tornadofx.borderpane
import tornadofx.button
import tornadofx.center
import tornadofx.circle
import tornadofx.datagrid
import tornadofx.drawer
import tornadofx.enableWhen
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import tornadofx.paddingLeft
import tornadofx.paddingTop
import tornadofx.pane
import tornadofx.right
import tornadofx.stackpane
import tornadofx.tab
import tornadofx.tabpane
import tornadofx.text
import tornadofx.textarea
import tornadofx.textfield
import tornadofx.vbox
import tornadofx.vgrow
import java.time.Duration
import java.util.Optional
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NotebookSpace : View() {

    val logger = KotlinLogging.logger {}
    private val tools = (1..10).toList()

    override val scope = super.scope as NotebookScope

    fun TabPane.tabPage(page: PageViewModel) {
        logger.info("adding tab for page: ${page.pageId}")
        val tab = tab(page.pageId) {
            borderpane {
                center {
                    vbox {
                        vbox {
                            grow()

                            codearea(page.fileContent) {
                                logger.info("ktScript initial: ${page.ktScript}")
                                var ast = page.ktScript?.let {
                                    KNConverter.convertScript(it)
                                    val highlighting = controller.computeHighlighting(it)
                                    controller.applyHighlighting(highlighting)
                                }
                                logger.info("ast: $ast")

//                                page.ktScriptProperty.addListener(ChangeListener { change, old, new ->
//                                    logger.info("ktScript change: $old -> $new")
//                                    if (new != null) {
//                                        ast = KNConverter.convertScript(new)
//                                        val highlighting = controller.computeHighlighting(new)
//                                        controller.applyHighlighting(highlighting)
//                                    }
//                                })

                                textProperty().addListener { _, _, new ->
                                    page.dirtyState = true
//                                                it.fileContent = new
                                    val pageManager = KNote.NOTEBOOK_MANAGER.pageManager
                                    pageManager.updateSourceCode(page.pageId, new)
                                }

                                grow()

//                                multiPlainChanges()
//                                    .successionEnds(Duration.ofMillis(500))
//                                    .supplyTask(controller::computeHighlightingAsync)
//                                    .awaitLatest(codeArea.multiPlainChanges())
//                                    .filterMap { t ->
//                                        if (t.isSuccess) {
//                                            Optional.of(codeArea.multiPlainChanges()
//                                                .successionEnds(Duration.ofMillis(500))
//                                                .supplyTask(controller::computeHighlightingAsync)
//                                                .awaitLatest(codeArea.multiPlainChanges())
//                                                .filterMap { Optional.of(t.get()) })
//                                        } else {
//                                            t.failure.printStackTrace()
//                                            Optional.of(codeArea.multiPlainChanges()
//                                                .successionEnds(Duration.ofMillis(500))
//                                                .supplyTask(controller::computeHighlightingAsync)
//                                                .awaitLatest(codeArea.multiPlainChanges())
//                                                .filterMap { Optional.empty<StyleSpans<Collection<String>>>() })
//                                        }
//                                    }
//                                    .subscribe { controller::applyHighlighting }

                            }
                            // TODO() redo results to accept any, check NikkyAi's branch update for that
                            vbox {
                                textarea(page.resultStringProperty) {
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
                                pane { hgrow = Priority.ALWAYS }
                                button("Force Rerun") {
                                    enableWhen(page.dirtyStateProperty)
                                    setOnAction {
                                        scope.pageManager.executePage(page.pageId)
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
        this.selectionModel.select(tab)
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
                if(change.wasUpdated()) {
                    logger.debug("updated: ${change}")
                }
                if(change.wasReplaced()) {
                    logger.debug("replaced: ${change}")
                }
                if(change.wasPermutated()) {
                    logger.debug("permutated: ${change}")
                }
            }
        })
        scope.pageViewModels.forEach { addedPage ->
            tabPage(addedPage)
        }
    }

    override fun onCreate() {
        super.onCreate()
        logger.info("clicked create")

        val pageId = SimpleStringProperty("new_page")
        dialog {
            label("page ID")
            textfield(pageId) {
                setOnKeyPressed { event ->
                    if (event.code == KeyCode.ENTER) {
                        this@dialog.close()
                        logger.info("creating new page '${pageId.value}'")
                        scope.pageManager.createPage(pageId.value)
                    }
                }
            }
        }
    }

    override fun onDelete() {
        logger.info("clicked delete")
        val text = root.selectionModel.selectedItem.text
        val toRemove = scope.pageViewModels.first { it.pageId == text }

        scope.pageManager.removePage(toRemove.pageId)
//        scope.pageViewModels.remove()
    }
}