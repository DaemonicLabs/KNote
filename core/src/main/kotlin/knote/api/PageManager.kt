package knote.api

import knote.script.PageScript
import knote.util.KObservableMap
import kotlin.script.experimental.api.ScriptDiagnostic

interface PageManager {
    val pages: KObservableMap<String, Page>

    @Deprecated("use knote.api.Page::compiledScript")
    val compiledPages: Map<String, PageScript>
        get() {
            return pages
                .mapValues { it.value.compiledScript }
                .filterValues { it != null }
                .mapValues { it.value!! }
        }
    @Deprecated("use knote.api.Page::results")
    val results: Map<String, Any>
        get() {
            return pages
                .mapValues { it.value.result }
                .filterValues { it != null }
                .mapValues { it.value!! }
        }

    @Deprecated("use knote.api.Page::results")
    val allResults: Map<String, Any>
        get() {
            return pages
                .mapValues { getResultOrExec(it.key) }
                .filterValues { it != null }
                .mapValues { it.value!! }
        }

    @Deprecated("use knote.api.Page::reports")
    val reportMap: Map<String, List<ScriptDiagnostic>>
        get() {
            return pages
                .mapValues { it.value.reports }
                .filterValues { it != null }
                .mapValues { it.value!! }
        }
    @Deprecated("use knote.api.Page::dependencies")
    val dependencies: Map<String, Set<String>>
        get() {
            return pages
                .mapValues { it.value.dependencies }
        }
    @Deprecated("use knote.api.Page::fileContent")
    val fileContentMap: Map<String, String>
        get() {
            return pages
                .mapValues { it.value.fileContent }
        }

    /**
     * Evaluate and Execute a page
     */
    fun evalPage(pageId: String): Pair<PageScript, Any?>?

    fun getResultOrExec(pageId: String): Any?

    /**
     * Execute Page without recompiling the script
     */
    fun execPage(pageId: String): Any?

    fun updateSourceCode(pageId: String, content: String)
    fun executeAll(): Map<String, Any>
}