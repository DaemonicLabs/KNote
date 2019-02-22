package knote

import knote.host.createJvmScriptingHost
import knote.host.evalScript
import knote.poet.NotePage
import knote.script.NotebookScript
import knote.tornadofx.model.Page
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.io.File

object KNote {
    //TODO: load, register and watch notebooks

    val cacheDir = File(System.getProperty("user.dir")).resolve("build").resolve(".knote-cache").apply { mkdirs() }
    private val host = createJvmScriptingHost(cacheDir)
    private val workingDir = File(System.getProperty("user.dir")).absoluteFile!!

    init {
        println("workingDir: $workingDir")
    }

    private val notebooksDir = File(System.getProperty("user.dir")).absoluteFile.resolve("notebooks")

    init {
        println("notebooksDir: $notebooksDir")
    }

    var notebookFilter: List<String>? = null

    val pageRegistries: MutableMap<String, PageRegistry> = mutableMapOf()
    val notebooks: MutableList<NotebookScript> = mutableListOf()

    fun addNewPageToNoteBookFromGUI(notebookId: String, page: Page) {
        val notebook = findNotebook(notebookId)
        val file = File("${page.name}.page.kts")

        file.printWriter().use { out -> out.println(page.script)}
        val notePage = NotePage(file, file.name)
        // add to notebook, then execute script to generate Pages and also add to the notebook
    }

    fun evalNotebooks() {
        val notebookFiles = notebooksDir.listFiles { file -> file.isFile && file.name.endsWith(".notebook.kts") }
        println("notebookFiles: $notebookFiles")

        notebookFiles
            .filter {
                notebookFilter?.let { filter ->
                    it.name.substringBeforeLast(".notebook.kts") in filter
                } ?: true
            }
            .forEach(::evalNotebook)
        notebooks.forEach { notebook ->
            val otherNotebooks = notebooks - notebook
            val otherIds = otherNotebooks.flatMap { it.includes.map { it.id } }
            val pageIds = notebook.includes.map { it.id }
            pageIds.forEach { id ->
                require(id !in otherIds) {
                    "page with id: $id is included in multiple notebooks"
                }
            }
        }
        startWatcher()
    }

    fun evalNotebook(file: File) {
        val id = file.name.substringBeforeLast(".notebook.kts")
        val notebook = host.evalScript<NotebookScript>(file, args = *arrayOf(id), libs = workingDir.resolve("libs"))

        pageRegistries[id] = PageRegistry(notebook, host)
    }

    fun findNotebook(id: String) = notebooks.find { it.id == id }

    fun startWatcher() {
        println("starting notebook watcher")
        runBlocking {
            watchActor(notebooksDir.toPath()) {
                for (watchEvent in channel) {
                    val path = watchEvent.context()
                    val file = path.toFile()
                    if (!file.name.endsWith(".notebook.kts")) continue
                    val matches = notebookFilter?.let { filter ->
                        file.name.substringBeforeLast(".notebook.kts") in filter
                    } ?: true
                    if (!matches) {
                        continue
                    }
                    when (watchEvent.kind().name()) {
                        "ENTRY_CREATE" -> {
                            println("$path was created")
                        }
                        "ENTRY_MODIFY" -> {
                            println("$path was modified")
                            // TODO: delete all pages and readd
                        }
                        "ENTRY_DELETE" -> {
                            println("$path was deleted")
                        }
                        "OVERFLOW" -> println("${watchEvent.context()} overflow")
                    }
                }
            }

        }
        println("started watcher")
    }

    private val jobs: MutableList<Job> = mutableListOf()
    fun cancelOnShutDown(job: Job) {
        jobs += job
    }

    fun shutdown() {
        println("doing shutdown")
        jobs.forEach { it.cancel() }
    }
}