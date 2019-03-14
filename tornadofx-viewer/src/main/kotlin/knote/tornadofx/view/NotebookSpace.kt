package knote.tornadofx.view

import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.TabPane
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.Font
import knote.KNote
import knote.tornadofx.Styles
import knote.tornadofx.controller.NotebookSpaceController
import knote.tornadofx.model.NotebookScope
import knote.tornadofx.model.PageViewModel
import knote.util.codearea
import mu.KotlinLogging
import org.fxmisc.richtext.CodeArea
import org.fxmisc.richtext.model.StyleSpans
import tornadofx.*
import java.time.Duration
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class NotebookSpace : View() {

    private val logger = KotlinLogging.logger {}
    private val tools = (1..10).toList()

    /**
     * TODO -
     * 1) Kotlin highlighting
     * 2) Remove "close" from tabs"
     * 3) Create a more flexible input area -- Diagram model design
     * 4) Jump to page dependencies and dependents at the drawer navigation
     * 5) List file imports
     * 6) Declare different result types - SPIKE
     *    - string
     *    - plot
     *    - pie chart
     *    - bar graph
     *    (study Jupyter to find other types of input)
     * 7) Restructure result output area -- Diagram model design
     * 8) Include file inputs as tabs (i.e. csv/editor)
     */

    var codeArea = CodeArea()
    var executor: ExecutorService = Executors.newSingleThreadExecutor()

    override val scope = super.scope as NotebookScope
    private val controller: NotebookSpaceController by inject()

    private fun Node.grow(priority: Priority = Priority.ALWAYS) {
        hgrow = priority
        vgrow = priority
    }

    private fun TabPane.tabPage(page: PageViewModel) {
        logger.info("adding tab for page: ${page.pageId}")
        tab(page.pageId) {
            borderpane {
                center {
                    vbox {
                        vbox {
                            grow()

                            codearea(page.fileContent) {
                                textProperty().addListener { _, _, new ->
                                    page.dirtyState = true
                                    val pageManager = KNote.NOTEBOOK_MANAGER.pageManager
                                    pageManager.updateSourceCode(page.pageId, new)
                                }

                                grow()

                                multiPlainChanges()
                                        .successionEnds(Duration.ofMillis(500))
                                        .supplyTask(controller::computeHighlightingAsync)
                                        .awaitLatest(codeArea.multiPlainChanges())
                                        .filterMap { t ->
                                            if (t.isSuccess) {
                                                Optional.of(codeArea.multiPlainChanges()
                                                        .successionEnds(Duration.ofMillis(500))
                                                        .supplyTask(controller::computeHighlightingAsync)
                                                        .awaitLatest(codeArea.multiPlainChanges())
                                                        .filterMap { Optional.of(t.get()) })
                                            } else {
                                                t.failure.printStackTrace()
                                                Optional.of(codeArea.multiPlainChanges()
                                                        .successionEnds(Duration.ofMillis(500))
                                                        .supplyTask(controller::computeHighlightingAsync)
                                                        .awaitLatest(codeArea.multiPlainChanges())
                                                        .filterMap { Optional.empty<StyleSpans<Collection<String>>>() })
                                            }
                                        }
                                        .subscribe { controller::applyHighlighting }

                            }

                            vbox {
                                textarea(page.resultStringProperty) {
                                    isEditable = false
                                    font = Font.font("monospaced", font.size)
                                    grow(Priority.SOMETIMES)
                                }
                                grow(Priority.SOMETIMES)
                                // minHeight = 280.0
                            }.addClass(Styles.evaluationConsole)

                            hbox {
                                pane { hboxConstraints { hGrow = Priority.ALWAYS } }
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
                            item("Page Dependencies") {
                                text("List of page dependencies here")
                            }
                        }
                    }
                }
            }
        }
    }

    override val root = tabpane {
        scope.pageViewModels.addListener(ListChangeListener { change ->
            while (change.next()) {
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

    override fun onCreate() {
        super.onCreate()
        logger.info("clicked create")

        val pageId = SimpleStringProperty("new_page")
        dialog {
            label("page ID")
            textfield (pageId) {
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
}