package knote

import knote.api.Notebook
import knote.api.NotebookManager
import knote.api.PageManager
import knote.data.NotebookImpl
import knote.host.EvalScript
import knote.script.NotebookScript
import knote.util.MutableKObservableObject
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging

internal object NotebookManagerImpl : NotebookManager, KLogging() {
    private val notebookScriptFile = KNote.notebookDir.resolve("${KNote.notebookId}.notebook.kts")
    override val notebookObject: MutableKObservableObject<NotebookManager, Notebook> = MutableKObservableObject(
        NotebookImpl(
            id = KNote.notebookId,
            file = notebookScriptFile
        )
    )
    override var notebook by notebookObject

    private val host = EvalScript.createJvmScriptingHost(KNote.cacheDir)

    init {
        startWatcher()
        compileNotebook()
    }

    /***
     * evaluates a single notebook file
     * and registers its pages
     */
    override fun compileNotebook(): Notebook? {
        val notebookId = KNote.notebookId
        logger.debug("attempting to construct notebook '$notebookId'")
        if (!notebookScriptFile.exists()) {
            logger.error("notebook: $notebookId does not exist ($notebookScriptFile)")
            return null
        }
//        val id = file.name.substringBeforeLast(".notebook.kts")
        val (notebookScript, reports) = EvalScript.evalScript<NotebookScript>(
            host,
            notebookScriptFile,
            args = *arrayOf(notebookId, KNote.notebookDir)
        )
        val notebook = notebook as NotebookImpl
        notebook.reports = reports
        notebook.compiledScript = notebookScript
        if (notebookScript == null) {
            logger.error("evaluation failed for notebook $notebookId")
            return notebook
        }

        if(notebook.pageManager == null) {
            notebook.pageManager = PageManagerImpl(notebook, host)
        }
        return notebook
    }

    override val pageManager: PageManager
    get() {
        val notebookId = KNote.notebookId
        val notebook = compileNotebookCached() as? NotebookImpl ?: run {
            logger.error("cannot load notebook $notebookId")
            throw IllegalStateException("cannot load notebook for $notebookId")
        }
        val pageManager = notebook.pageManager.let {
            it ?: PageManagerImpl(notebook, host).also {
                notebook.pageManager = it
            }
        }
        return pageManager
    }

    private fun invalidateNotebook() {
        val notebook = notebookObject.value as NotebookImpl
        notebook.compiledScript = null
        notebook.reports = null
        val pageManager = notebook.pageManager as PageManagerImpl
        pageManager.stopWatcher()
//        notebook.pageManager = null
    }

    override fun compileNotebookCached() = notebookObject.value.takeIf {
        it.compiledScript != null
    } ?: run {
        compileNotebook()
    }

    private var watchJob: Job? = null
    private fun startWatcher() {
        logger.debug("starting notebook watcher")
        watchJob?.cancel()
        val job = watchActor(KNote.notebookDir.toPath()) {
            var timeout: Job? = null
            for (watchEvent in channel) {
                val path = watchEvent.context()
                val file = path.toFile()
                val event = watchEvent.kind()
                if (!file.name.endsWith(".notebook.kts")) continue
                val id = file.name.substringBeforeLast(".notebook.kts")
                if (id != KNote.notebookId) continue
                timeout?.cancel()
                timeout = launch {
                    PageManagerImpl.logger.info("event: $path, ${event.name()}")
                    delay(1000)

                    when (watchEvent.kind().name()) {
                        "ENTRY_CREATE" -> {
                            logger.debug("$path was created")
                            compileNotebook()
                        }
                        "ENTRY_MODIFY" -> {
                            logger.debug("$path was modified")
                            // TODO: delete all pages and readd
                            invalidateNotebook()
                            compileNotebook()
                        }
                        "ENTRY_DELETE" -> {
                            logger.debug("$path was deleted")
                            invalidateNotebook()
                        }
                        "OVERFLOW" -> logger.debug("${watchEvent.context()} overflow")
                    }
                }
            }
        }
        KNote.cancelOnShutDown(job)
        watchJob = job
        logger.trace("started notebook watcher")
    }
}
