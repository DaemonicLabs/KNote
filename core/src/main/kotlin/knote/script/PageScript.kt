package knote.script

import knote.KNote
import knote.api.Notebook
import knote.api.PageResult
import knote.data.PageImpl
import mu.KLogging
import mu.KotlinLogging
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
    val logger = KotlinLogging.logger(id)

    companion object : KLogging()

    override fun toString() = "PageScript(id=$id)"

    open fun process(): Any? {
        return null
    }

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
                val depReturnType = pageManager.resultType(dependencyId)!!
                require(depReturnType.isSubtypeOf(property.returnType)) {
                    "$depReturnType} is not assignable to ${property.returnType}"
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