package knote

import knote.host.createJvmScriptingHost
import knote.host.evalScript
import knote.script.NotebookScript
import java.io.File
import kotlin.system.exitProcess

fun main(vararg args: String) {
    // TODO: start tornadofx application
    // TODO: file change listener

    KNote.notebooks.forEach { notebook ->
        val pageRegistry = KNote.pageRegistries[notebook.id]!!
        pageRegistry.allResults.forEach { pageId, result ->
            println("[$pageId]: KClass: ${result::class} value: '$result'")
        }
    }
}