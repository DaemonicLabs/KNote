package knote

import knote.api.NotebookManager
import knote.api.PageManager
import knote.util.KObservableMap
import knote.util.MutableKObservableMap
import kotlinx.coroutines.Job
import mu.KLogging
import java.io.File

object KNote : KLogging() {
    private val workingDir = File(System.getProperty("user.dir")).absoluteFile!!
    private val jobs: MutableList<Job> = mutableListOf()

    init {
        logger.info("workingDir: $workingDir")
    }

    @Deprecated("use knote.api.Notebook::pageManager")
    val PAGE_REGISTRIES: KObservableMap<String, PageManager> = MutableKObservableMap()
    val NOTEBOOK_MANAGER: NotebookManager = NotebookManagerImpl

    fun cancelOnShutDown(job: Job) {
        jobs += job
    }

    fun shutdown() {
        logger.info("doing shutdown")
        jobs.forEach { it.cancel() }
    }
}