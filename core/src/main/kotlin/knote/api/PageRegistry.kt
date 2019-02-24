package knote.api

import knote.script.PageScript
import knote.util.KObservableMap

interface PageRegistry {

    val compiledPages: KObservableMap<String, PageScript>
    val results: KObservableMap<String, Any>
    val allResults: Map<String, Any>

    /**
     * Evaluate and Execute a page
     */
    fun evalPage(pageId: String): Pair<PageScript, Any?>?

    fun getResultOrExec(pageId: String): Any?

    /**
     * Execute Page without recompiling the script
     */
    fun execPage(pageId: String): Any?
}