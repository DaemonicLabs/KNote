package knote

import knote.api.NotebookManager
import kotlinx.coroutines.Job
import mu.KLogging
import java.io.File

object KNote : KLogging() {
    val notebookDir = System.getProperty("knote.notebookDir")?.let { File(it) }
        ?: throw IllegalStateException("no -Dknote.notebookDir passed")
    val rootDir = File(System.getProperty("user.dir")!!)
    val notebookId = System.getProperty("knote.id") ?: run {
        throw IllegalStateException("no -Dknote.id passed")
    }

    private val jobs: MutableList<Job> = mutableListOf()

    val cacheDir = rootDir
        .resolve("build")
        .resolve(".knote-cache")
        .apply { mkdirs() }

    init {
        logger.info("notebookDir: $notebookDir")
    }

    // TODO: merge notebook manager into KNote
    val NOTEBOOK_MANAGER: NotebookManager = NotebookManagerImpl

    fun cancelOnShutDown(job: Job) {
        jobs += job
    }

    fun shutdown() {
        logger.info("doing shutdown")
        jobs.forEach { it.cancel() }
    }
}