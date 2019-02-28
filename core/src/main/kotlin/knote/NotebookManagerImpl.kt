package knote

import knote.api.Notebook
import knote.api.NotebookManager
import knote.api.PageManager
import knote.data.NotebookImpl
import knote.host.EvalScript
import knote.script.NotebookScript
import knote.util.MutableKObservableMap
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

internal object NotebookManagerImpl : NotebookManager, KLogging() {
    override val notebooks: MutableKObservableMap<String, Notebook> = MutableKObservableMap()

    val cacheDir = File(System.getProperty("user.dir"))
        .resolve("build")
        .resolve(".knote-cache")
        .apply { mkdirs() }

    private val host = EvalScript.createJvmScriptingHost(cacheDir)
    private val workingDir = File(System.getProperty("user.dir")).absoluteFile!!

    private val notebooksDir = File(System.getProperty("user.dir")).absoluteFile.resolve("notebooks").apply {
        mkdirs()
        logger.info("notebooksDir: $this")
    }

    override val reportMap: MutableKObservableMap<String, List<ScriptDiagnostic>> = MutableKObservableMap()
    override val compiledNotebooks: MutableKObservableMap<String, NotebookScript> = MutableKObservableMap()

    override var notebookFilter: List<String>? = null

    override val listNotebookFiles: Array<out File>
        get() = notebooksDir.listFiles { file ->
            file.isFile && file.name.endsWith(".notebook.kts")
        }

    override fun evalNotebooks() {
        logger.info("listNotebookFiles: $listNotebookFiles")

        listNotebookFiles
            .map { it.name.substringBeforeLast(".notebook.kts") }
            .filter { id ->
                notebookFilter?.let { filter -> id in filter } ?: true
            }
            .forEach {
                NotebookManagerImpl.evalNotebook(it)
            }
        startWatcher()
    }

    /***
     * evaluates a single notebook file
     * and registers its pages
     */
    override fun evalNotebook(notebookId: String): Notebook? {
        if (notebookFilter?.let { notebookId !in it } == true) {
            logger.error("$notebookId rejected by notebookFilter: $notebookFilter")
            return null
        }
        val file = notebooksDir.resolve("$notebookId.notebook.kts")
        val id = file.name.substringBeforeLast(".notebook.kts")
        val (notebookScript, reports) = EvalScript.evalScript<NotebookScript>(
            host,
            file,
            args = *arrayOf(id, workingDir)
        )
        val notebook = notebooks.getOrPut(id) {
            NotebookImpl(
                id = id,
                file = file
            )
        } as NotebookImpl
        notebook.reports = reports
        notebook.compiledScript = notebookScript
        if (notebookScript == null) {
            println("evaluation failed for notebook $id")
            return notebook
        }

        notebook.pageManager = PageManagerImpl(notebook, host, workingDir)
        return notebook
    }

    override fun getPageManager(notebookId: String): PageManager? {
        val notebook = notebooks[notebookId] as NotebookImpl
        val oldManager = notebook.pageManager
        if(oldManager == null) {
            notebook.pageManager = PageManagerImpl(notebook, host, workingDir)
        }
        return notebook.pageManager
    }

    private fun invalidateNotebook(id: String) {
        val notebook = notebooks[id]?.let { it as NotebookImpl } ?: return
        notebook.compiledScript = null
        notebook.reports = null
        val oldRegistry = notebook.pageManager?.let { it as PageManagerImpl }
        oldRegistry?.stopWatcher()
        notebook.pageManager = null
    }

    override fun findNotebook(notebookId: String) = notebooks[notebookId] ?: run {
        evalNotebook(notebookId)
    }

    private var watchJob: Job? = null
    private fun startWatcher() {
        logger.debug("starting notebook watcher")
        watchJob?.cancel()
        watchJob = watchActor(notebooksDir.toPath()) {
            var timeout: Job? = null
            for (watchEvent in channel) {
                val path = watchEvent.context()
                val file = path.toFile()
                val event = watchEvent.kind()
                val id = file.name.substringBeforeLast(".notebook.kts")
                if (!file.name.endsWith(".notebook.kts")) continue
                val matches = notebookFilter?.let { filter ->
                    id in filter
                } ?: true
                if (!matches) {
                    continue
                }
                timeout?.cancel()
                timeout = launch {
                    PageManagerImpl.logger.info("event: $path, ${event.name()}")
                    delay(1000)

                    when (watchEvent.kind().name()) {
                        "ENTRY_CREATE" -> {
                            logger.debug("$path was created")
                            evalNotebook(id)
                        }
                        "ENTRY_MODIFY" -> {
                            logger.debug("$path was modified")
                            // TODO: delete all pages and readd
                            invalidateNotebook(id)
                            evalNotebook(id)
                        }
                        "ENTRY_DELETE" -> {
                            logger.debug("$path was deleted")
                            invalidateNotebook(id)
                        }
                        "OVERFLOW" -> logger.debug("${watchEvent.context()} overflow")
                    }
                }
            }
        }
        logger.debug("started notebook watcher")
    }
}
