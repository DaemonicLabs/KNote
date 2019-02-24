package knote

import knote.annotations.FromPage
import knote.host.evalScript
import knote.poet.PageMarker
import knote.script.NotebookScript
import knote.script.PageScript
import knote.util.MapLike
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class PageRegistry(
    val notebook: NotebookScript,
    val host: BasicJvmScriptingHost
) {
    val pages: MutableMap<String, PageScript> = mutableMapOf()
    private val resultsMap: MutableMap<String, Any> = mutableMapOf()
    val dependencies: MutableMap<String, MutableSet<String>> = mutableMapOf()
    val reportMap: MutableMap<String, List<ScriptDiagnostic>> = mutableMapOf()

    init {
        notebook.pageFiles.forEach(::evalPage)

        // TODO: add file watcher for pages
        startWatcher()

        while((pages.keys - resultsMap.keys).isNotEmpty()) {
            val pageIds = pages.keys - resultsMap.keys
            pageIds.forEach { id ->
                execPage(id)
            }
        }
    }


    fun evalPage(file: File) {
        val id = file.name.substringBeforeLast(".page.kts")
        require(file.exists()) {
            "page: $id does not exist ($file)"
        }
        val (page , reports) = host.evalScript<PageScript>(
            file,
            id,
            libs = File("libs")
        )
        reportMap[id] = reports
        if(page == null) {
            println("evaluation failed")
            return
        }
        pages[id] = page

        if(id in resultsMap)
            resultsMap.remove(id)
        execPage(id)
    }

    fun removePage(id: String) {
        pages.remove(id)
        resultsMap.remove(id)
        dependencies.remove(id)
//        dependencies.forEach { dependency, dependents ->
//            dependents -= id
//        }
    }

    val result: MapLike<String, Any?> = object: MapLike<String, Any?> {
        override operator fun get(key: String): Any? {
            return getResultOrExec(key)
        }
    }

    val allResults: Map<String, Any>
    get() {
        return pages.keys.associate { id ->
            val result = getResultOrExec(id) ?: throw IllegalStateException("could not evaluate result for '${id}'")
            id to result
        }
    }

    fun updateResult(pageId: String, result: Any) {
        val oldResult = resultsMap[pageId]
        if (oldResult == null || oldResult != result)
            resultsMap[pageId] = result

        val continuations = dependencies[pageId]

        continuations?.forEach {
            execPage(it)
        }
    }

    private fun execPage(pageId: String): Any? {
        val page = pages[pageId] ?: run {
            println("page $pageId not evaluated yet")
            return null
        }
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
            dependencies.getOrPut(pageId) { mutableSetOf() } += sourceId
            getResultOrExec(sourceId) ?: return null
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

    fun getResultOrExec(pageId: String): Any? = resultsMap[pageId] ?: execPage(pageId)

    private var watchJob: Job? = null
    fun startWatcher() {
        runBlocking {
            watchJob = watchActor(File("pages").absoluteFile.toPath()) {
                for (watchEvent in channel) {
                    val path = watchEvent.context()
                    val file = path.toFile()
                    val id = file.name.substringBeforeLast(".page.kts")
                    val notePage = notebook.includes.find { it.id == id } ?: continue
                    when (watchEvent.kind().name()) {
                        "ENTRY_CREATE" -> {
                            println("${watchEvent.context()} was created")
                            evalPage(notePage)
                        }
                        "ENTRY_MODIFY" -> {
                            println("${watchEvent.context()} was modified")
                            removePage(id)
                            evalPage(notePage)
                        }
                        "ENTRY_DELETE" -> {
                            println("${watchEvent.context()} was deleted")
                            removePage(id)
                        }
                        "OVERFLOW" -> println("${watchEvent.context()} overflow")
                    }
                }
            }

        }
    }

    fun stopWatcher() {
        watchJob?.cancel()
        watchJob = null
    }
}