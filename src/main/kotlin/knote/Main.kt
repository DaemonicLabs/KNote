package knote

import knote.host.createJvmScriptingHost
import knote.host.evalScript
import knote.script.NotebookScript
import java.io.File
import kotlin.system.exitProcess

fun main(vararg args: String) {
    // TODO: file change listener

    val scriptPath = args.getOrNull(0) ?: run {
        System.err.println("no script file passed")
        exitProcess(-1)
    }

    // use a cacheDir in the OS specific directories in production PLEASE
    val cacheDir = File(System.getProperty("user.dir")).resolve(".cache")
    cacheDir.mkdirs()

    val host = createJvmScriptingHost(cacheDir)
    val scriptFile = File(System.getProperty("user.dir")).parentFile.resolve("notebooks").resolve(scriptPath).absoluteFile

    val workingDir = File(System.getProperty("user.dir")).absoluteFile!!
    val notebook = host.evalScript<NotebookScript>(scriptFile, args = *arrayOf(arrayOf(workingDir.path)))

    println("notebook: $notebook")

    val pageRegistry = PageRegistry(notebook, host)

    val firstResult = pageRegistry.getResultOrEval("one")
    println("one: $firstResult")

//    val id = scriptFile.name.substringBeforeLast(".knote.kts")
//    scriptEnv.doThings(id)
}