package knote

import knote.host.evalScript
import knote.script.NotebookScript
import knote.script.PageScript
import java.io.File
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class PageRegistry(
    var notebook: NotebookScript,
    val host: BasicJvmScriptingHost
) {
    private val resultsMap: MutableMap<String, Any> = mutableMapOf()

    fun updateResult(pageId: String, result: Any) {
        val oldResult = resultsMap[pageId]
        if(oldResult == null || oldResult != result)
            resultsMap[pageId] = result

        val continuations = notebook.dependencies.filter { it.first.id == pageId }.map { it.second.id }

        continuations.forEach {
            evaluatePage(it)
        }
    }

    private fun evaluatePage(pageId: String): Any {
        val scriptFile = File(System.getProperty("user.dir")).parentFile.resolve("pages").resolve("$pageId.page.kts")
        val inputs = notebook.dependencies.filter { it.second.id == pageId }
            .map {
                getResultOrEval(it.first.id)
            }

        val page = host.evalScript<PageScript>(scriptFile, args = *inputs.toTypedArray())
        val result =  page.result
        updateResult(pageId, result)
        return result
    }
    fun getResultOrEval(pageId: String): Any = resultsMap[pageId] ?: evaluatePage(pageId)

}