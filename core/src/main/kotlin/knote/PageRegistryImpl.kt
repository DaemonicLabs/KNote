package knote

import javafx.collections.FXCollections
import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import knote.annotations.FromPage
import knote.api.PageRegistry
import knote.host.evalScript
import knote.script.NotebookScript
import knote.script.PageScript
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.valueParameters
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

internal class PageRegistryImpl(
    val notebook: NotebookScript,
    val host: BasicJvmScriptingHost
) : PageRegistry {
    override val compiledPages: ObservableMap<String, PageScript> = FXCollections.observableHashMap()
    override val results: ObservableMap<String, Any> = FXCollections.observableHashMap()


    override val dependencies: ObservableMap<String, ObservableSet<String>> = FXCollections.observableHashMap()
    override val reportMap: ObservableMap<String, List<ScriptDiagnostic>> = FXCollections.observableHashMap()

    init {
        notebook.pageFiles.forEach {
            evalPage(it)
        }

        startWatcher()

        while((compiledPages.keys - results.keys).isNotEmpty()) {
            val pageIds = compiledPages.keys - results.keys
            pageIds.forEach { id ->
                execPage(id)
            }
        }
    }

    override fun evalPage(pageId: String): Pair<PageScript, Any?>? {
        val file = notebook.fileForPage(pageId) ?: return null
        return evalPage(file, pageId)
    }

    fun evalPage(file: File, id: String = file.name.substringBeforeLast(".page.kts")): Pair<PageScript, Any?>? {
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
            return null
        }
        compiledPages[id] = page

        if(id in results)
            results.remove(id)
        return page to execPage(id)
    }

    private fun invalidatePage(id: String) {
        compiledPages.remove(id)
        results.remove(id)
        dependencies.remove(id)
//        dependencies.forEach { dependency, dependents ->
//            dependents -= id
//        }
    }

    override val allResults: Map<String, Any>
    get() {
        return compiledPages.keys.associate { id ->
            val result = getResultOrExec(id) ?: throw IllegalStateException("could not evaluate result for '${id}'")
            id to result
        }
    }

    private fun updateResult(pageId: String, result: Any) {
        val oldResult = results[pageId]
        if (oldResult == null || oldResult != result)
            results[pageId] = result

        val continuations = dependencies[pageId]

        continuations?.forEach {
            execPage(it)
        }
    }

    override fun execPage(pageId: String): Any? {
        val page = compiledPages[pageId] ?: run {
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
            dependencies.getOrPut(pageId) { FXCollections.observableSet() } += sourceId
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

    override fun getResultOrExec(pageId: String): Any? = results[pageId] ?: execPage(pageId)

    private var watchJob: Job? = null
    private fun startWatcher() {
        runBlocking {
            watchJob = watchActor(notebook.pageRoot.absoluteFile.toPath()) {
                for (watchEvent in channel) {
                    val path = watchEvent.context()
                    val file = notebook.pageRoot.resolve(path.toFile()).absoluteFile
                    if(!file.name.endsWith(".page.kts")) continue
                    val id = file.name.substringBeforeLast(".page.kts")
                    when (watchEvent.kind().name()) {
                        "ENTRY_CREATE" -> {
                            println("${watchEvent.context()} was created")
                            evalPage(file)
                        }
                        "ENTRY_MODIFY" -> {
                            println("${watchEvent.context()} was modified")
                            invalidatePage(id)
                            evalPage(file)
                        }
                        "ENTRY_DELETE" -> {
                            println("${watchEvent.context()} was deleted")
                            invalidatePage(id)
                        }
                        "OVERFLOW" -> println("${watchEvent.context()} overflow")
                    }
                }
            }

        }
    }

    internal fun stopWatcher() {
        watchJob?.cancel()
        watchJob = null
    }
}