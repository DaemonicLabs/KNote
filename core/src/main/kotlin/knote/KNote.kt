package knote

import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import knote.api.NotebookRegisty
import knote.api.PageRegistry
import kotlinx.coroutines.Job
import mu.KLogging
import java.io.File

object KNote: KLogging() {
    private val workingDir = File(System.getProperty("user.dir")).absoluteFile!!

    val cacheDir = File(System.getProperty("user.dir")).resolve("build").resolve(".knote-cache").apply { mkdirs() }


    init {
        logger.info("workingDir: $workingDir")
    }

    val pageRegistries: ObservableMap<String, PageRegistry> = FXCollections.observableHashMap()
    val notebookRegistry: NotebookRegisty = NotebookRegistryImpl

    private val jobs: MutableList<Job> = mutableListOf()
    fun cancelOnShutDown(job: Job) {
        jobs += job
    }

    fun shutdown() {
        logger.info("doing shutdown")
        jobs.forEach { it.cancel() }
    }
}