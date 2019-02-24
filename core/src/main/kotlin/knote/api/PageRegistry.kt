package knote.api

import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import knote.script.PageScript
import kotlin.script.experimental.api.ScriptDiagnostic

interface PageRegistry {

    val compiledPages: ObservableMap<String, PageScript>
    val results: ObservableMap<String, Any>

    val allResults: Map<String, Any>

    val reportMap: ObservableMap<String, List<ScriptDiagnostic>>
    val dependencies: ObservableMap<String, ObservableSet<String>>

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