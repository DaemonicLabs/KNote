package knote.tornadofx.view

import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import knote.KNote
import knote.tornadofx.model.NotebookModel
import knote.tornadofx.model.NotebookScope
import knote.tornadofx.model.PageViewModel
import knote.util.BindingUtil
import knote.util.asObservable
import mu.KotlinLogging
import tornadofx.*
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import java.util.stream.IntStream
import java.util.stream.Stream

class NotebookWorkbench : Workspace() {
    val logger = KotlinLogging.logger {}

    override fun onBeforeShow() {
        KNote.NOTEBOOK_MANAGER.compileNotebookCached()
        logger.info("id: ${KNote.notebookId}")
        val pageManager = KNote.NOTEBOOK_MANAGER.pageManager

        val pageViewModels: ObservableList<PageViewModel> = observableList()

        BindingUtil.mapContent(pageViewModels, pageManager.pages.asObservable) { pageId, page ->
            val result = pageManager.executePageCached(pageId)
            logger.info("[$pageId]: ${result?.let { "KClass: ${it::class}" }} value: '$result'")

            // add any pageViewModels from any input .csv files
            page.fileInputs.forEach { file ->
                if (file.endsWith(".csv")) {
                    csvToPageViewModel(file)
                }
            }

            PageViewModel(page).also { logger.debug("mapped '$pageId'") }
        }


        pageViewModels.addListener(ListChangeListener { change ->
            while(change.next()) {
                if (change.wasAdded()) {
                    logger.info("added: ${change.addedSubList}")
                }
                if (change.wasRemoved()) {
                    logger.info("removed: ${change.removed}")
                }
            }
        })

        val notebookModel = NotebookModel(KNote.NOTEBOOK_MANAGER.notebook, pageManager, pageViewModels)

        val notebookScope = NotebookScope(
                notebookModel.notebook,
                notebookModel.pageManager,
                notebookModel.pageViewModels
        )

        workspace.dock<NotebookSpace>(notebookScope)
    }

    private fun csvToPageViewModel(path: Path) {
        val headers = readCsvHeaders(path)
        val result = listOf<Map<String, String>>()

        lateinit var stream: Stream<String>
        try {
            stream = Files.lines(path)
            /*result = stream
                    .skip(1)
                    .map { line -> line.split(",") }
                    .map { data -> IntStream.range(0, data.size)
                            .boxed()
                    }
                    .collect(Collectors.toList())*/ // TODO fix I got tired
        } catch (e: IOException) {
            throw e
        }



    }

    private fun readCsvHeaders(path: Path): List<String> {
        val reader = Files.newBufferedReader(path)
        return reader.readLine().split(",")
    }
}
