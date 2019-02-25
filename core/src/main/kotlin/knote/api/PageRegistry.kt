package knote.api

import javafx.collections.ObservableMap
import javafx.collections.ObservableSet
import knote.script.PageScript
import knote.util.KObservableMap
import kotlin.script.experimental.api.ScriptDiagnostic

interface PageRegistry {

    val compiledPages: KObservableMap<String, PageScript>
    val results: KObservableMap<String, Any>

    val allResults: Map<String, Any>

    val reportMap: KObservableMap<String, List<ScriptDiagnostic>>
    val dependencies: KObservableMap<String, Set<String>>

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

}