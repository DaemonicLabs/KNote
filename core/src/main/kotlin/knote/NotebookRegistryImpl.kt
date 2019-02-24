package knote

import knote.api.NotebookRegisty
import knote.host.createJvmScriptingHost
import knote.host.evalScript
import knote.script.NotebookScript
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.io.File

object NotebookRegistryImpl : NotebookRegisty {
    private val host = createJvmScriptingHost(KNote.cacheDir)
    private val workingDir = File(System.getProperty("user.dir")).absoluteFile!!

    private val notebooksDir = File(System.getProperty("user.dir")).absoluteFile.resolve("notebooks").apply {
        mkdirs()
        println("notebooksDir: $this")
    }

    override var notebookFilter: List<String>? = null

    override val notebookFiles: Array<out File>
        get() = notebooksDir.listFiles { file ->
            file.isFile && file.name.endsWith(".notebook.kts")
        }

    override val notebooks: MutableList<NotebookScript> = mutableListOf()

    override fun evalNotebooks() {
        println("notebookFiles: $notebookFiles")

        notebookFiles
            .map { it.name.substringBeforeLast(".notebook.kts") }
            .filter { id ->
                notebookFilter?.let { filter -> id in filter } ?: true
            }
            .forEach {
                NotebookRegistryImpl.evalNotebook(it)
            }
        startWatcher()
    }

    /***
     * evaluates a single notebook file
     * and registers its pages
     */
    override fun evalNotebook(notebookId: String): NotebookScript? {
        if(notebookFilter?.let { notebookId !in it } == true) {
            println("$notebookId rejected by notebookFilter: $notebookFilter")
            return null
        }
        val file = notebooksDir.resolve("$notebookId.notebook.kts")
        val id = file.name.substringBeforeLast(".notebook.kts")
        val (notebook, reports) = host.evalScript<NotebookScript>(
            file,
            args = *arrayOf(id, workingDir),
            libs = workingDir.resolve("libs")
        )
        KNote.reportMap[id] = reports
        if(notebook == null) {
            println("evaluation failed")
            return null
        }

        notebooks += notebook
        KNote.pageRegistries[id] = PageRegistryImpl(notebook, host)
        return notebook
    }

    private fun invalidateNotebook(id: String) {
        val notebook = findNotebook(id) ?: return
        notebooks -= notebook
        val oldRegistry = KNote.pageRegistries[id]!! as PageRegistryImpl
        oldRegistry.stopWatcher()
        KNote.pageRegistries.remove(id)
    }

    override fun findNotebook(notebookId: String) = notebooks.find { it.id == notebookId }

    private var watchJob: Job? = null
    private fun startWatcher() {
        println("starting notebook watcher")
        runBlocking {
            watchJob?.cancel()
            watchJob = watchActor(notebooksDir.toPath()) {
                for (watchEvent in channel) {
                    val path = watchEvent.context()
                    val file = path.toFile()
                    val id = file.name.substringBeforeLast(".notebook.kts")
                    if (!file.name.endsWith(".notebook.kts")) continue
                    val matches = notebookFilter?.let { filter ->
                        id in filter
                    } ?: true
                    if (!matches) {
                        continue
                    }
                    when (watchEvent.kind().name()) {
                        "ENTRY_CREATE" -> {
                            println("$path was created")
                            evalNotebook(id)
                        }
                        "ENTRY_MODIFY" -> {
                            println("$path was modified")
                            // TODO: delete all pages and readd
                            invalidateNotebook(id)
                            evalNotebook(id)
                        }
                        "ENTRY_DELETE" -> {
                            println("$path was deleted")
                            invalidateNotebook(id)
                        }
                        "OVERFLOW" -> println("${watchEvent.context()} overflow")
                    }
                }
            }

        }
        println("started watcher")
    }
}
