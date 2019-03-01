package knote.api

import knote.script.PageScript
import knote.util.KObservableMap
import kotlin.script.experimental.api.ScriptDiagnostic

interface PageManager {
    val pages: KObservableMap<String, Page>

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