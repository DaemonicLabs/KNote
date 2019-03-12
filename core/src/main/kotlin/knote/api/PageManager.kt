package knote.api

import knote.PageManagerImpl
import knote.script.PageScript
import knote.util.KObservableMap
import java.io.File
import kotlin.reflect.KType

interface PageManager {
    val pages: KObservableMap<String, Page>

    /**
     * Compiles a page
     */
    fun compilePageCached(pageId: String): Page?

    fun compilePage(pageId: String): Page?

    /**
     * Execute Page without recompiling the script
     */
    fun executePageCached(pageId: String): Any?

    fun executePage(pageId: String): Any?

    fun updateSourceCode(pageId: String, content: String)

    fun executeAll(): Map<String, Any>

    fun resultType(pageId: String): KType?
    fun setPageListener(listener: PageManagerImpl.PageListener)
    fun watchDataFile(id: String, file: File)
}