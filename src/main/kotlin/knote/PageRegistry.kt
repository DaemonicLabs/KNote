package knote

import knote.annotations.FromPage
import knote.host.evalScript
import knote.script.NotebookScript
import knote.script.PageScript
import knote.util.MapLike
import knote.util.watchActor
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class PageRegistry(
    val notebook: NotebookScript,
    val host: BasicJvmScriptingHost
) {
    var pages: List<PageScript>
    init {
        pages = notebook.includes.map {
            host.evalScript<PageScript>(it.file, it.id, libs = File("libs"))
        }

        // TODO: add file watcher for pages
        startWatcher()
    }

    val result: MapLike<String, Any?> = object: MapLike<String, Any?> {
        override operator fun get(key: String): Any? {
            return getResultOrEval(key)
        }
    }

    private val resultsMap: MutableMap<String, Any> = mutableMapOf()
    val dependencies: MutableMap<String, MutableSet<String>> = mutableMapOf()

    val allResults: Map<String, Any>
    get() {
        return pages.associate {
            val result = getResultOrEval(it.id) ?: throw IllegalStateException("could not evaluate result for '${it.id}'")
            it.id to result
        }
    }

    fun updateResult(pageId: String, result: Any) {
        val oldResult = resultsMap[pageId]
        if (oldResult == null || oldResult != result)
            resultsMap[pageId] = result

//        val continuations = notebook.dependencies.filter { it.first.id == pageId }.map { it.second.id }
//
//        continuations.forEach {
//            evaluatePage(it)
//        }
    }

    private fun evaluatePage(pageId: String): Any? {
        val scriptFile = File(System.getProperty("user.dir")).resolve("pages").resolve("$pageId.page.kts")
        require(scriptFile.exists()) {
            "page: $pageId does not exist ($scriptFile)"
        }

        val page = host.evalScript<PageScript>(scriptFile, pageId, libs = File("libs").absoluteFile)
//        page::class.members.filterIsInstance(KMutableProperty::class.java).forEach {
//            if(it.isLateinit) {
//                val depId = it.name
//                val result = getResultOrEval(depId)
//                it.setter.call(page, result)
//            }
//        }
        val processFunction = page::class.declaredFunctions.find {
            it.name == "process"
        } ?: run {
            println("no function `process` found")
            return null
        }
        val parameters = processFunction.valueParameters.map { parameter ->
            println("parameter: $parameter")
            println("parameter.name: ${parameter.name}")
            println("parameter.annotations: ${parameter.annotations}")
            val pageResult = parameter.findAnnotation<FromPage>()
            require(pageResult != null) { "parameter: ${parameter.name} is not annotated with PageResult" }
            val sourceId = pageResult.source.takeIf { it.isNotBlank() } ?: parameter.name!!
            // TODO: register page as dependent on sourceId
            dependencies.getOrPut(page.id) { mutableSetOf() } += sourceId
            getResultOrEval(sourceId)
        }
        println("arguments: $parameters")
        val result = processFunction.call(page, *parameters.toTypedArray())
//        val result = page.process()
        println("result of '$pageId': '$result'")
        if (result == null) {
            println("process function returned null")
            return null
        }
        updateResult(pageId, result)
        return result
    }

    fun getResultOrEval(pageId: String): Any? = resultsMap[pageId] ?: evaluatePage(pageId)

    fun startWatcher() {
        runBlocking {
            watchActor(File("pages").absoluteFile.toPath()) {
                for (watchEvent in channel) {
                    when (watchEvent.kind().name()) {
                        "ENTRY_CREATE" -> println("${watchEvent.context()} was created")
                        "ENTRY_MODIFY" -> println("${watchEvent.context()} was modified")
                        "OVERFLOW" -> println("${watchEvent.context()} overflow")
                        "ENTRY_DELETE" -> println("${watchEvent.context()} was deleted")
                    }
                }
            }

        }
    }
}