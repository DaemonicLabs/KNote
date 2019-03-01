package knote.script

import knote.KNote
import knote.api.Notebook
import knote.api.PageResult
import knote.data.PageImpl
import mu.KLogging
import java.io.File
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.isSubtypeOf
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "Notebook Page",
    fileExtension = "page.kts",
    compilationConfiguration = PageConfiguration::class
)
open class PageScript(
    val notebook: Notebook,
    val id: String,
    val rootDir: File
) {
    companion object : KLogging()

    override fun toString() = "PageScript(id=$id)"

    open fun process(): Any? {
        return null
    }

//    fun <T> fromPage(pageId: String): T {
//        logger.debug("notebook: $notebook")
//        val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager(notebookId = notebook.id)!!
//        //TODO: add typecheck
//        logger.debug("getting result for $pageId")
//        val result = pageManager.getResultOrExec(pageId)!!
//        val page = pageManager.pages[pageId]!!
//        logger.debug("result: $pageId = $result")
//        logger.debug("result::class: ${result::class}")
//        page.dependencies as MutableSet += pageId
//        return result as T
////        if(result is T)
////            return result
////        else {
////            throw IllegalStateException("result: ${result::class} is not ${T::class}")
////        }
//    }

    fun <This, T> This.inject(pageId: String? = null): PageResult<This, T> {
        val delegate = object : PageResult<This, T> {
            override fun getValue(self: This, property: KProperty<*>): T {
                val dependencyId = pageId ?: property.name
                logger.debug("property: ${property.name}")
                logger.debug("notebook: $notebook")
//                val notebook = KNote.NOTEBOOK_MANAGER.evalNotebook(notebook.id)!!
                logger.debug("notebook.pageManager: ${notebook.pageManager}")
                val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager(notebook.id)!!
                logger.debug("notebook.pageManager: ${notebook.pageManager}")
                //TODO: add typecheck
                val result = pageManager.getResultOrExec(dependencyId)!!
                val depPage = pageManager.pages[dependencyId]!!
                val page = pageManager.pages[id]!!
                logger.debug("result: $result")
                logger.debug("result::class: ${result::class}")
                // find and match result type
                val dependencyProcessFunction = depPage.compiledScript!!::class.declaredFunctions
                    .find { it.name == "process" }
                require (dependencyProcessFunction != null) {
                    "cannot find  ::process in page '$dependencyId'"
                }
                require(dependencyProcessFunction.returnType.isSubtypeOf(property.returnType)) {
                    "${dependencyProcessFunction.returnType} is not assignable to ${property.returnType}"
                }
                logger.debug("adding dependency $dependencyId to ${page.id}")
                (page as PageImpl).dependencies += dependencyId
                logger.debug("dependencies of $id: ${depPage.dependencies}")
                return result as? T ?: run {
                    throw IllegalStateException("result: ${result::class} is not ${property.returnType}")
                }
            }
        }
        logger.debug("created delegate for $pageId")
        return delegate
    }

    // TODO: visualize data
}