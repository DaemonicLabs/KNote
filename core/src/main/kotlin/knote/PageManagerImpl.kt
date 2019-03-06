package knote

import knote.api.Page
import knote.api.PageManager
import knote.data.NotebookImpl
import knote.data.PageImpl
import knote.host.EvalScript
import knote.host.EvalScript.posToString
import knote.script.PageScript
import knote.util.MutableKObservableMap
import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KLogging
import java.io.File
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class PageManagerImpl(
    private val notebook: NotebookImpl,
    private val host: BasicJvmScriptingHost,
    private val workingDir: File
) : PageManager {

    companion object : KLogging()

    val notebookScript get() = notebook.compiledScript!!

    private var pageListener: PageListener? = null

    override val pages: MutableKObservableMap<String, Page> = MutableKObservableMap()

    init {
        // init all pages (without compilation)
        notebookScript.pageFiles.forEach { file ->
            val id = file.name.substringBeforeLast(".page.kts")
            pages.getOrPut(id) {
                PageImpl(id, file)
            }
        }

        startWatcher()

        // loop over all pages that are evaluated but have no result yet
    }

    // provide a way for another class to set the listener
    override fun setPageListener(listener: PageListener) {
        this.pageListener = listener
    }

    interface PageListener {
        fun onResultsUpdated(result: Any?)
    }

    override fun executeAll(): Map<String, Any> {
        notebookScript.pageFiles.forEach {
            val id = it.name.substringBeforeLast(".page.kts")
            pages[id]?.compiledScript ?: evalPage(it)
        }
        while (pages.any { (id, page) -> page.result == null && !(page as PageImpl).errored }) {
            pages.filterValues { it.result == null }
                .forEach { id, page ->
                    id to getResultOrExec(id)
                }
        }
        return pages.mapValues { (id, page) ->
            getResultOrExec(id) ?: "errored: ${(page as PageImpl).errored}"
        }
    }

    override fun resultType(pageId: String): KType? {
        val page = findPage(pageId) ?: run {
            logger.warn("could not find page $pageId")
            return null
        }
        val pageScript = page.compiledScript ?: run {
            logger.warn("script for page $pageId is not compiled")
            evalPage(pageId)?.compiledScript ?: return null
        }
        val processFunction =
            pageScript::class.declaredMemberFunctions.find { it.name == "process" } ?: run {
                logger.error("no function `process` found in $pageId")
                return null
            }
        return processFunction.returnType
    }

    override fun findPage(pageId: String): Page? = pages[pageId] ?: run {
        evalPage(pageId) ?: run {
            logger.warn("evalPage($pageId) returned null")
            null
        }
    }

    override fun evalPage(pageId: String): Page? {
        val file = notebookScript.fileForPage(pageId, logger) ?: return null
        return evalPage(file, pageId)
    }

    fun evalPage(file: File, id: String = file.name.substringBeforeLast(".page.kts")): Page? {
        require(file.exists()) {
            "page: $id does not exist ($file)"
        }
        val page = pages.getOrPut(id) {
            PageImpl(id, file)
        } as PageImpl
        val (pageScript, reports) = EvalScript.evalScript<PageScript>(
            host,
            file,
            args = *arrayOf(notebook, id, workingDir)
        )
        page.fileContent = file.readText()
        page.reports = reports
        page.compiledScript = pageScript
        if (pageScript == null) {
            logger.error("evaluation failed for file $file")
            reports.forEach { report ->
                val path = report.sourcePath?.let { "$it: " } ?: ""
                val location = report.location?.posToString()?.let { "$it: " } ?: ""
                val messageString = "$path$location${report.message}"
                when (report.severity) {
                    ScriptDiagnostic.Severity.FATAL -> logger.error { "FATAL: $messageString" }
                    ScriptDiagnostic.Severity.ERROR -> logger.error { messageString }
                    ScriptDiagnostic.Severity.WARNING -> logger.warn { messageString }
                    ScriptDiagnostic.Severity.INFO -> logger.info { messageString }
                    ScriptDiagnostic.Severity.DEBUG -> logger.debug { messageString }
                }
                report.exception?.apply {
                    logger.error(message, this)
                    this.cause?.apply {
                        logger.error(message, this)
                    }
                    this.suppressed.forEach {
                        logger.error("suppressed exception: ${it.message}", this)
                    }
                }
            }
            page.errored = true
            return null
        }

        page.errored = false
        page.result = null

        return page
    }

    private fun invalidatePage(id: String): Set<String>? {
        val page = pages[id] as? PageImpl ?: return null
        page.compiledScript = null

        invalidateResult(id)
        return page.dependencies
//        dependencies.forEach { dependency, dependents ->
//            dependents -= id
//        }
    }

    private fun invalidateResult(pageId: String) {
        val page = pages[pageId] as PageImpl
        logger.debug("invalidating result for '$pageId'")
//        logger.debug("dependencies of $pageId: ${page.dependencies}")
        page.result = null

        // find all pages depending on this page and invalidate them too
        pages.forEach { depId, depPage ->
            //            logger.debug("dependencies of $depId: ${depPage.dependencies}")
//            if(depId == pageId) return@forEach
            if (pageId in depPage.dependencies) {
                invalidateResult(depId)
            }
        }
        // remove all old dependencies this page had
        page.dependencies = setOf()
    }

    private fun updateResult(pageId: String, result: Any) {
        val page = pages[pageId] as PageImpl
        val oldResult = page.result
        if (oldResult == null || oldResult != result)
            page.result = result

        val continuations = page.dependencies

        continuations.forEach {
            getResultOrExec(it)
        }
    }

    override fun execPage(pageId: String): Any? {
        val page = pages[pageId] as PageImpl
        val pageScript = page.compiledScript ?: run {
            logger.debug("page $pageId not evaluated yet")
            evalPage(pageId)?.compiledScript ?: return null
        }
//        val processFunction = pageScript::class.declaredFunctions.find {
//            it.name == "process"
//        } ?: run {
//            logger.debug("no function `process` found")
//            return null
//        }

//        val parameters = processFunction.parameters.associate { parameter ->
//            logger.debug("parameter: $parameter")
//            logger.debug("parameter.name: ${parameter.name}")
//            logger.debug("parameter.annotations: ${parameter.annotations}")
//            logger.debug("parameter.type: ${parameter.type}")
//            when (parameter.kind) {
//                KParameter.Kind.INSTANCE -> {
//                    return@associate parameter to pageScript
//                }
//                else -> {
//                }
//            }
//            val fromPage = parameter.findAnnotation<FromPage>()
//            require(fromPage != null) { "parameter: ${parameter.name} is not annotated with PageResult" }
//            val paramId = fromPage.source.takeIf { it.isNotBlank() } ?: parameter.name!!
//            page.dependencies as MutableSet += paramId
//            val paramResult = getResultOrExec(paramId) ?: return null
//            // require result to be assignable to the parameter
//            val paramPage = pages[paramId] ?: return null
//            val parameterScript = paramPage.compiledScript ?: return null
//            val parameterProcessFunc =
//                parameterScript::class.declaredMemberFunctions.find { it.name == "process" } ?: run {
//                    logger.error("no function `process` found in $paramId")
//                    return null
//                }
//            require(parameterProcessFunc.returnType.isSubtypeOf(parameter.type)) {
//                logger.error("${parameterProcessFunc.returnType} is not a subtype of requested ${parameter.type}")
//                logger.error("on page: $pageId")
//                logger.info("parameter script: ${parameterScript.id}")
//                logger.info("expected type: ${parameter.type}")
//                logger.info("return type: ${parameterProcessFunc.returnType}")
//                logger.info("parameter process function: $parameterProcessFunc")
//                println()
//                page.compiledScript = null
//                "${parameterProcessFunc.returnType} is not a subtype of requested ${parameter.type}"
//            }
        // TODO: ensure actual type of paramResult is accessible
//            val paramClass = paramResult::class
//            logger.debug("paramResult::class: $paramClass")
//            logger.debug("paramResult::class.visibility : ${paramClass.visibility}")
//            if(paramClass.visibility == KVisibility.INTERNAL) {
//                val proxyResult = ProxyUtil.createProxy(paramResult, parameter.type.jvmErasure.java)
//                logger.debug("paramResult::class: ${proxyResult::class}")
//                return@associate parameter to proxyResult
//            }
//
//            parameter to (paramResult)
//        }
        logger.debug("executing '$pageId'")
//        logger.debug("arguments: $parameters")
//        logger.debug("arguments: ${parameters.values.map { it::class }}")
        val result = try {
            pageScript.process()
//            processFunction.callBy(parameters)
//            processFunction.call(parameters.values)
        } catch (e: Exception) {
            logger.error("executing process function failed", e)
            page.errored = true
            return null
        }
//        val result = page.process()
        logger.debug("result of '$pageId': '$result'")
        if (result == null) {
            logger.error("process function returned null")
            page.errored = true
            return null
        }
        updateResult(pageId, result)
        return result
    }

    override fun getResultOrExec(pageId: String): Any? {
        val page = pages[pageId] as? PageImpl ?: run {
            evalPage(pageId) ?: return null
        }

        pageListener?.onResultsUpdated(page.result)
        return page.result ?: execPage(pageId)
    }

    override fun updateSourceCode(pageId: String, content: String) {
        val file = notebookScript.fileForPage(pageId, logger) ?: return
        file.writeText(content)
    }

    private var watchJob: Job? = null
    private fun startWatcher() {
        NotebookManagerImpl.logger.debug("starting page watcher")
        watchJob = watchActor(notebookScript.pageRoot.absoluteFile.toPath()) {
            var timeout: Job? = null
            for (watchEvent in channel) {
                val path = watchEvent.context()
                val file = notebookScript.pageRoot.resolve(path.toFile()).absoluteFile
                val event = watchEvent.kind()
                if (!file.name.endsWith(".page.kts")) continue

                timeout?.cancel()
                timeout = launch {
                    logger.info("event: $path, ${event.name()}")
                    delay(1000)

                    val id = file.name.substringBeforeLast(".page.kts")
                    when (event.name()) {
                        "ENTRY_CREATE" -> {
                            logger.debug("${watchEvent.context()} was created")
                            evalPage(file)
                        }
                        "ENTRY_MODIFY" -> {
                            logger.debug("${watchEvent.context()} was modified")
                            invalidatePage(id)
                            logger.debug("invalidated pages: ${pages.filterValues { it.result == null }.keys}}")
                            evalPage(file)
                            // ensure all pages have their results cached again
                            notebookScript.pageFiles.forEach {
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
            }
        }

        NotebookManagerImpl.logger.trace("started page watcher")
    }

    internal fun stopWatcher() {
        watchJob?.cancel()
        watchJob = null
    }
}