package knote

import knote.api.NotebookManager
import knote.api.PageManager
import knote.util.KObservableMap
import knote.util.MutableKObservableMap
import kotlinx.coroutines.Job
import mu.KLogging
import java.io.File

object KNote: KLogging() {
    private val workingDir = File(System.getProperty("user.dir")).absoluteFile!!
    private val jobs: MutableList<Job> = mutableListOf()

    val cacheDir = File(System.getProperty("user.dir"))
            .resolve("build")
            .resolve(".knote-cache")
            .apply { mkdirs() }

    init {
        logger.info("workingDir: $workingDir")
    }

    val PAGE_REGISTRIES: KObservableMap<String, PageManager> = MutableKObservableMap()
    val NOTEBOOK_REGISTRY: NotebookManager = NotebookManagerImpl

    fun cancelOnShutDown(job: Job) {
        jobs += job
    }

    fun shutdown() {
        logger.info("doing shutdown")
        jobs.forEach { it.cancel() }
    }

}