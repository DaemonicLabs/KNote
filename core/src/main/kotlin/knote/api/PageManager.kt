package knote.api

import knote.PageManagerImpl
import knote.script.PageScript
import knote.util.KObservableMap
import kotlin.reflect.KType
import kotlin.script.experimental.api.ScriptDiagnostic

interface PageManager {
    val pages: KObservableMap<String, Page>

    /**
     * Evaluate and Execute a page
     */
    fun evalPage(pageId: String): Page?

    fun getResultOrExec(pageId: String): Any?

    /**
     * Execute Page without recompiling the script
     */
    fun execPage(pageId: String): Any?

    fun updateSourceCode(pageId: String, content: String)
    fun executeAll(): Map<String, Any>
    fun resultType(pageId: String): KType?
    fun findPage(pageId: String): Page?
    fun setPageListener(listener: PageManagerImpl.PageListener)
}