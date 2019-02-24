package knote.api

import knote.script.PageScript
import java.io.File

interface PageRegistry {

    val compiledPages: Map<String, PageScript>

    val results: Map<String, Any>
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