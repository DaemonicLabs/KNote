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

internal class PageManagerImpl(
    private val notebook: NotebookImpl,
    private val host: BasicJvmScriptingHost
) : PageManager {
    companion object : KLogging()

    val notebookScript get() = notebook.compiledScript!!

    override val pages: MutableKObservableMap<String, Page> = MutableKObservableMap()

    init {
        // init all pages (without compilation)
        notebookScript.pageFiles.forEach { file ->
            val id = file.name.substringBeforeLast(".page.kts")
            pages.getOrPut(id) {
                PageImpl(id, file)
            }
        }

        startPageWatcher()
        startDataWatcher()
    }

    override fun executeAll(): Map<String, Any> {
        notebookScript.pageFiles.forEach {
            val id = it.name.substringBeforeLast(".page.kts")
            pages[id]?.compiledScript ?: compilePage(it)
        }
        while (pages.any { (id, page) -> page.result == null && !(page as PageImpl).errored }) {
            pages.filterValues { it.result == null }
                .forEach { id, page ->
                    id to executePageCached(id)
                }
        }
        return pages.mapValues { (id, page) ->
            executePageCached(id) ?: "errored: ${(page as PageImpl).errored}"
        }
    }

    override fun resultType(pageId: String): KType? {
        val page = compilePageCached(pageId) ?: run {
            logger.warn("could not find page $pageId")
            return null
        }
        val pageScript = page.compiledScript ?: run {
            logger.warn("script for page $pageId is not compiled")
            compilePage(pageId)?.compiledScript ?: return null
        }
        val processFunction =
            pageScript::class.declaredMemberFunctions.find { it.name == "process" } ?: run {
                logger.error("no function `process` found in $pageId")
                return null
            }
        return processFunction.returnType
    }

    override fun compilePageCached(pageId: String): Page? = pages[pageId] ?: run {
        compilePage(pageId) ?: run {
            logger.warn("compilePage($pageId) returned null")
            null
        }
    }

    override fun compilePage(pageId: String): Page? {
        val file = notebookScript.fileForPage(pageId, logger) ?: return null
        return compilePage(file, pageId)
    }

    fun compilePage(file: File, id: String = file.name.substringBeforeLast(".page.kts")): Page? {
        require(file.exists()) {
            "page: $id does not exist ($file)"
        }
        val page = pages.getOrPut(id) {
            PageImpl(id, file)
        } as PageImpl
        val (pageScript, reports) = EvalScript.evalScript<PageScript>(
            host,
            file,
            args = *arrayOf(notebook, id, KNote.notebookDir)
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
        if(page.text != pageScript.text) {
            page.text = pageScript.text
        }
        return page
    }

    private fun invalidatePage(id: String): Set<String>? {
        val page = pages[id] as? PageImpl ?: return null
        page.compiledScript?.invalidate()
        page.compiledScript = null
        page.text = ""
        page.fileInputs.clear()

        invalidateResult(id)
        return page.dependencies
    }

    private fun invalidateResult(pageId: String) {
        val page = pages[pageId] as PageImpl
        logger.debug("invalidating result for '$pageId'")
        page.result = null

        // find all pages depending on this page and invalidate them too
        pages.forEach { depId, depPage ->
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
            executePageCached(it)
        }
    }

    override fun executePage(pageId: String): Any? {
        val page = pages[pageId] as PageImpl
        val pageScript = page.compiledScript ?: run {
            logger.debug("page $pageId not evaluated yet")
            compilePage(pageId)?.compiledScript ?: return null
        }
        logger.debug("executing '$pageId'")
        val result = try {
            pageScript.process()
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

    override fun executePageCached(pageId: String): Any? {
        val page = pages[pageId] as? PageImpl ?: run {
            compilePage(pageId) ?: return null
        }
        return page.result ?: executePage(pageId)
    }

    override fun updateSourceCode(pageId: String, content: String) {
        val file = notebookScript.fileForPage(pageId, logger) ?: return
        file.writeText(content)
    }

    override fun watchDataFile(pageId: String, file: File) {
        val page = pages[pageId] as? PageImpl ?: run {
            throw IllegalStateException("page $pageId cannot be loaded as PageImpl")
        }
        val relative = file.relativeTo(notebookScript.dataRoot)
        val path = relative.toPath()
        page.fileInputs.add(path)
    }

    private var watchPageJob: Job? = null
    private fun startPageWatcher() {
        logger.debug("starting page watcher")
        val job = watchActor(notebookScript.pageRoot.absoluteFile.toPath()) {
            var timeout: Job? = null
            for (watchEvent in channel) {
                val path = watchEvent.context()
                val file = notebookScript.pageRoot.resolve(path.toFile()).absoluteFile
                val event = watchEvent.kind()
                if (!file.name.endsWith(".page.kts")) continue

                logger.info("event: $path, ${event.name()}")
                timeout?.cancel()
                timeout = launch {
                    delay(1000)

                    val id = file.name.substringBeforeLast(".page.kts")
                    when (event.name()) {
                        "ENTRY_CREATE" -> {
                            logger.debug("${watchEvent.context()} was created")
                            compilePage(file)
                        }
                        "ENTRY_MODIFY" -> {
                            logger.debug("${watchEvent.context()} was modified")
                            invalidatePage(id)
                            logger.debug("invalidated pages: ${pages.filterValues { it.result == null }.keys}}")
                            compilePage(file)
                            // ensure all pages have their results cached again
                            notebookScript.pageFiles.forEach {
                                val id = it.name.substringBeforeLast(".page.kts")
                                val result = executePageCached(id)
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
        KNote.cancelOnShutDown(job)
        watchPageJob = job

        logger.trace("started page watcher")
    }
    private var watchDataJob: Job? = null

    private fun startDataWatcher() {
        logger.debug("starting page watcher")
        val job = watchActor(notebookScript.dataRoot.absoluteFile.toPath()) {
            var timeout: Job? = null
            for (watchEvent in channel) {
                val path = watchEvent.context()
                val file = notebookScript.dataRoot.resolve(path.toFile()).absoluteFile
                val event = watchEvent.kind()

                logger.info("event: $path, ${event.name()}")


                if(event.name() == "ENTRY_DELETE" || event.name() == "OVERFLOW") continue
                // TODO: readd editing timeout
                timeout?.cancel()
                timeout = launch {
                    delay(1000)

                    when (event.name()) {
                        "ENTRY_CREATE" -> {
                            logger.debug("${watchEvent.context()} was created")
                            pages.forEach { pageId, page ->
                                if(path in page.fileInputs) {
                                    executePage(pageId)
                                }
                            }
                        }
                        "ENTRY_MODIFY" -> {
                            logger.debug("${watchEvent.context()} was modified")
                            var executed = false
                            pages.forEach { pageId, page ->
                                if(path in page.fileInputs) {
                                    executePage(pageId)
                                    executed = true
                                }
                            }
                            // ensure all pages have their results cached again
                            if(executed) {
                                notebookScript.pageFiles.forEach {
                                    val id = it.name.substringBeforeLast(".page.kts")
                                    val result = executePageCached(id)
                                    logger.info("[$id] => $result")
                                }
                            }
                        }
                        "ENTRY_DELETE" -> {
                            logger.debug("${watchEvent.context()} was deleted")
                        }
                        "OVERFLOW" -> logger.debug("${watchEvent.context()} overflow")
                    }
                }
            }
        }
        KNote.cancelOnShutDown(job)
        watchPageJob = job

        logger.trace("started data watcher")
    }
    internal fun stopWatcher() {
        watchPageJob?.cancel()
        watchPageJob = null
        watchDataJob?.cancel()
        watchDataJob = null
    }
}