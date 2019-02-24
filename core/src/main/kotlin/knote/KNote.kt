package knote

import knote.api.NotebookRegisty
import knote.api.PageRegistry
import knote.host.createJvmScriptingHost
import knote.host.evalScript
import knote.script.NotebookScript
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

object KNote {
    private val workingDir = File(System.getProperty("user.dir")).absoluteFile!!

    val cacheDir = File(System.getProperty("user.dir")).resolve("build").resolve(".knote-cache").apply { mkdirs() }

    val reportMap: MutableMap<String, List<ScriptDiagnostic>> = mutableMapOf()

    init {
        println("workingDir: $workingDir")
    }

    val pageRegistries: MutableMap<String, PageRegistry> = mutableMapOf()
    val notebookRegistry: NotebookRegisty = NotebookRegistryImpl

    private val jobs: MutableList<Job> = mutableListOf()
    fun cancelOnShutDown(job: Job) {
        jobs += job
    }

    fun shutdown() {
        println("doing shutdown")
        jobs.forEach { it.cancel() }
    }
}