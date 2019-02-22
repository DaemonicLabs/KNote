package knote

import knote.host.createJvmScriptingHost
import knote.host.evalScript
import knote.script.NotebookScript
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

    private val notebookFiles = notebooksDir.listFiles { file -> file.isFile && file.name.endsWith(".notebook.kts") }
    init {
        println("notebookFiles: $notebookFiles")
    }

    val notebooks = notebookFiles.map { file ->
        val id = file.name.substringBeforeLast(".notebook.kts")
        host.evalScript<NotebookScript>(file, args = *arrayOf(id), libs = workingDir.resolve("libs"))
    }
    init {
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
    }
    val pageRegistries = notebooks.associate {
        it.id to PageRegistry(it, host)
    }

    fun findNotebook(id: String) =notebooks.find { it.id == id }


}