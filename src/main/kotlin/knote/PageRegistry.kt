package knote

import knote.host.evalScript
import knote.script.NotebookScript
import knote.script.PageScript
import org.jetbrains.kotlin.utils.addToStdlib.cast
import java.io.File
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredFunctions
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class PageRegistry(
    var notebook: NotebookScript,
    val host: BasicJvmScriptingHost
) {
    private val resultsMap: MutableMap<String, Any> = mutableMapOf()

    fun updateResult(pageId: String, result: Any) {
        val oldResult = resultsMap[pageId]
        if (oldResult == null || oldResult != result)
            resultsMap[pageId] = result

//        val continuations = notebook.dependencies.filter { it.first.id == pageId }.map { it.second.id }
//
//        continuations.forEach {
//            evaluatePage(it)
//        }
    }

    private fun evaluatePage(pageId: String): Any? {
        val scriptFile = File(System.getProperty("user.dir")).parentFile.resolve("pages").resolve("$pageId.page.kts")

        val page = host.evalScript<PageScript>(scriptFile, pageId)
        val processFunction = page::class.declaredFunctions.find {
            it.name == "process"
        }
        page::class.members.filterIsInstance(KMutableProperty::class.java).forEach {
            if(it.isLateinit) {
                val depId = it.name
                val result = getResultOrEval(depId)
                it.setter.call(page, result)
            }
        }
//        if (processFunction == null) {
//            println("no function `process` found")
//            return null
//        }
//        val parameters = processFunction.valueParameters.map { parameter ->
//            println("parameter: $parameter")
//            println("parameter.annotations: ${parameter.annotations}")
//            val pageResult = parameter.findAnnotation<PageResult>()
////            val pageResult = parameter.annotations.find { it is PageResult } as? PageResult
//            require(pageResult != null) { "parameter: ${parameter.name} is not annotated with PageResult" }
//            getResultOrEval(pageResult.source)
//        }
//        println("arguments: $parameters")
//        val result = processFunction.call(*parameters.toTypedArray())
        val result = page.process()
        println("result of $pageId: $result")
        if (result == null) {
            println("process function returned null")
            return null
        }
        updateResult(pageId, result)
        return result
    }

    fun getResultOrEval(pageId: String): Any? = resultsMap[pageId] ?: evaluatePage(pageId)

}