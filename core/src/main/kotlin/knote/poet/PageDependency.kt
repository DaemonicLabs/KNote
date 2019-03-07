package knote.poet

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asTypeName
import knote.KNote
import knote.api.PageResult
import knote.data.PageImpl
import knote.script.PageScript
import mu.KLogging
import java.io.File
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf

object PageDependency : KLogging() {
    fun generate(
        output: File,
        notebookId: String,
        pageId: String,
        pageDependencies: Map<String, KType>
    ): File {
        val fileName = "dependencies_$pageId"
        val filespec = FileSpec.builder(packageName = "", fileName = fileName).apply {
            pageDependencies.forEach { depId, dependendcyType ->
                addProperty(
                    PropertySpec.builder(depId, dependendcyType.asTypeName()).apply {
                        delegate(
                            CodeBlock.of(
                                "%T.injectFromNotebook(notebookId=%S, pageId=%S)",
                                PageDependency::class,
                                notebookId,
                                pageId
                            )
                        )
                    }.build()
                )
            }
        }.build()

        filespec.writeTo(output)

        return output.resolve("$fileName.kt")
    }

    fun <T> injectFromNotebook(notebookId: String, pageId: String): PageResult<Nothing?, T> =
        object : PageResult<Nothing?, T> {
            override fun getValue(self: Nothing?, property: KProperty<*>): T {
                val dependencyId = property.name
                val notebook = KNote.NOTEBOOK_MANAGER.compileNotebookCached()
                require(notebook != null) { "cannot find notebook $notebookId" }
                PageScript.logger.debug("property: ${property.name}")
                PageScript.logger.debug("notebook: $notebook")
                val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager()!!
                PageScript.logger.debug("notebook.pageManager: ${notebook.pageManager}")
                val result = pageManager.executePageCached(dependencyId)!!
                val depPage = pageManager.pages[dependencyId]!!
                val page = pageManager.pages[pageId]!!
                PageScript.logger.debug("result: $result")
                PageScript.logger.debug("result::class: ${result::class}")
                // find and match result type
                val depReturnType = pageManager.resultType(dependencyId)!!
                require(depReturnType.isSubtypeOf(property.returnType)) {
                    "$depReturnType} is not assignable to ${property.returnType}"
                }
                PageScript.logger.debug("adding dependency $dependencyId to ${page.id}")
                (page as PageImpl).dependencies += dependencyId
                PageScript.logger.debug("dependencies of ${page.id}: ${page.dependencies}")
                return result as? T ?: run {
                    throw IllegalStateException("result: ${result::class} is not ${property.returnType}")
                }
            }
        }.also {
            PageScript.logger.debug("created delegate for $pageId")
        }
}
