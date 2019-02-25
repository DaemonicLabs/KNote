package knote

import knote.annotations.FromPage
import knote.api.PageRegistry
import knote.host.EvalScript
import knote.script.NotebookScript
import knote.script.PageScript
import knote.util.MutableKObservableMap
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging
import java.io.File
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KParameter
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

internal class PageRegistryImpl(
    val notebook: NotebookScript,
    val host: BasicJvmScriptingHost
) : PageRegistry {
    companion object : KLogging()

    override val compiledPages: MutableKObservableMap<String, PageScript> = MutableKObservableMap()
    override val results: MutableKObservableMap<String, Any> = MutableKObservableMap()

    override val dependencies: MutableKObservableMap<String, Set<String>> = MutableKObservableMap()
    override val reportMap: MutableKObservableMap<String, List<ScriptDiagnostic>> = MutableKObservableMap()
    override val fileContentMap: MutableKObservableMap<String, String> = MutableKObservableMap()


    init {
        notebook.pageFiles.forEach {
            evalPage(it)
        }

        startWatcher()

        while ((compiledPages.keys - results.keys).isNotEmpty()) {
            val pageIds = compiledPages.keys - results.keys
            pageIds.forEach { id ->
                getResultOrExec(id)
            }
        }
    }

    override fun evalPage(pageId: String): Pair<PageScript, Any?>? {
        val file = notebook.fileForPage(pageId, logger) ?: return null
        return evalPage(file, pageId)
    }

    fun evalPage(file: File, id: String = file.name.substringBeforeLast(".page.kts")): Pair<PageScript, Any?>? {
        require(file.exists()) {
            "page: $id does not exist ($file)"
        }
        val (page, reports) = EvalScript.evalScript<PageScript>(
            host,
            file,
            id,
            libs = File("libs")
        )
        fileContentMap[id] = file.readText()
        reportMap[id] = reports
        if (page == null) {
            logger.error("evaluation failed for file $file")
            reports.forEach {
                logger.error { it }
            }
            return null
        }
        compiledPages[id] = page

        if (id in results)
            results.remove(id)
        return page to execPage(id)
    }

    private fun invalidatePage(id: String): Set<String>? {
        compiledPages.remove(id)

        invalidateResult(id)
        return dependencies[id]
//        dependencies.forEach { dependency, dependents ->
//            dependents -= id
//        }
    }

    private fun invalidateResult(id: String) {
        logger.debug("invalidating result for '$id'")
        logger.debug("dependencies: $dependencies")
        results.remove(id)

        dependencies.forEach { depId, dependents ->
            if (dependents.contains(id))
                invalidateResult(depId)
        }

//        dependencies.remove(id)
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
            getResultOrExec(it)
        }
    }

    override fun execPage(pageId: String): Any? {
        val page = compiledPages[pageId] ?: run {
            logger.debug("page $pageId not evaluated yet")
            return null
        }
        val processFunction = page::class.declaredFunctions.find {
            it.name == "process"
        } ?: run {
            logger.debug("no function `process` found")
            return null
        }
        val parameters = processFunction.parameters.associate { parameter ->
            logger.debug("parameter: $parameter")
            logger.debug("parameter.name: ${parameter.name}")
            logger.debug("parameter.annotations: ${parameter.annotations}")
            when(parameter.kind) {
                KParameter.Kind.INSTANCE -> {
                    return@associate parameter to page
                }
                else -> {}
            }
            val pageResult = parameter.findAnnotation<FromPage>()
            require(pageResult != null) { "parameter: ${parameter.name} is not annotated with PageResult" }
            val paramId = pageResult.source.takeIf { it.isNotBlank() } ?: parameter.name!!
            dependencies.getOrPut(pageId) { mutableSetOf() } as MutableSet += paramId
            val paramResult = getResultOrExec(paramId) ?: return null
            // TODO: require result to be assignable to the parameter
            val parameterScript = compiledPages[paramId] ?: return null
            val parameterProcessFunc = parameterScript::class.declaredMemberFunctions.find { it.name == "process" }?: run {
                logger.error("no function `process` found in $paramId")
                return null
            }
            require(parameterProcessFunc.returnType.isSubtypeOf(parameter.type)) {
                logger.error("${parameterProcessFunc.returnType} is not a subtype of requested ${parameter.type}")
                logger.error("on page: $pageId")
                logger.info("parameter script: ${parameterScript.id}")
                logger.info("expected type: ${parameter.type}")
                logger.info("return type: ${parameterProcessFunc.returnType}")
                logger.info("parameter process function: $parameterProcessFunc")
                println()
                "${parameterProcessFunc.returnType} is not a subtype of requested ${parameter.type}"
            }

            parameter to paramResult
        }
        logger.debug("executing '$pageId' arguments: $parameters")
        val result = processFunction.callBy(parameters)
//        val result = page.process()
        logger.debug("result of '$pageId': '$result'")
        if (result == null) {
            logger.error("process function returned null")
            return null
        }
        updateResult(pageId, result)
        return result
    }

    override fun getResultOrExec(pageId: String): Any? = results[pageId] ?: execPage(pageId)

    override fun updateSourceCode(pageId: String, content: String) {
        val file = notebook.fileForPage(pageId, logger) ?: return
        file.writeText(content)
    }

    private var watchJob: Job? = null
    private fun startWatcher() {
        NotebookRegistryImpl.logger.debug("starting page watcher")
        watchJob = watchActor(notebook.pageRoot.absoluteFile.toPath()) {
            val fileRef: AtomicReference<File> = AtomicReference()
            val eventKind: AtomicReference<WatchEvent.Kind<Path>> = AtomicReference()
            var timeout: Job? = null

            for (watchEvent in channel) {
                val path = watchEvent.context()
                if (!path.toFile().name.endsWith(".page.kts")) continue

                timeout?.cancel()
                timeout = launch {
                    logger.info("event: $path, ${watchEvent.kind().name()}")
                    delay(1000)

                    val file = fileRef.get()
                    val id = file.name.substringBeforeLast(".page.kts")
                    when (eventKind.get().name()) {
                        "ENTRY_CREATE" -> {
                            logger.debug("${watchEvent.context()} was created")
                            evalPage(file)
                        }
                        "ENTRY_MODIFY" -> {
                            logger.debug("${watchEvent.context()} was modified")
                            invalidatePage(id)
                            logger.debug("results: $results")
                            evalPage(file)
                            // ensure all pages have their results cached again
                            notebook.pageFiles.forEach {
                                val id = it.name.substringBeforeLast(".page.kts")
                                val result = getResultOrExec(id)
                                logger.info("[$id] => $result")
                            }
                        }
                        "ENTRY_DELETE" -> {
                            logger.debug("${watchEvent.context()} was deleted")
                            invalidatePage(id)
                        }
                        "OVERFLOW" -> logger.debug("${watchEvent.context()} overflow")
                    }
                }

                fileRef.set(notebook.pageRoot.resolve(path.toFile()).absoluteFile)
                eventKind.set(watchEvent.kind())
                logger.info("set file and even refs")
            }
        }

        NotebookRegistryImpl.logger.debug("started page watcher")
    }

    internal fun stopWatcher() {
        watchJob?.cancel()
        watchJob = null
    }
}