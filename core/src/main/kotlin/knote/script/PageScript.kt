package knote.script

import knote.KNote
import knote.api.DelegatedResult
import knote.api.Notebook
import knote.data.PageImpl
import knote.isSubDirectoryOf
import knote.md.KNTextBuilder
import mu.KLogging
import mu.KotlinLogging
import java.io.File
import kotlin.reflect.KProperty
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
    val notebookDir: File
) {
    val logger = KotlinLogging.logger(id)

    val dataFolder = notebookDir.resolve("data")
    var text: String = ""
        private set

    companion object : KLogging()

    override fun toString() = "PageScript(id=$id)"

    open fun process(): Any? {
        return null
    }

    /**
     * TO be used from within the page script
     */
    val cachedResult: Any?
        get() {
            val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager() ?: return null
            return pageManager.executePageCached(id)!!
        }

    fun <This, T> This.inject(pageId: String? = null): DelegatedResult<This, T> {
        val delegate = object : DelegatedResult<This, T> {
            override fun getValue(self: This, property: KProperty<*>): T {
                val dependencyId = pageId ?: property.name
                logger.debug("property: ${property.name}")
                logger.debug("notebook: $notebook")
//                val notebook = KNote.NOTEBOOK_MANAGER.compileNotebook(notebook.id)!!
                logger.debug("notebook.pageManager: ${notebook.pageManager}")
                val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager()!!
                logger.debug("notebook.pageManager: ${notebook.pageManager}")
                //TODO: add typecheck
                val result = pageManager.executePageCached(dependencyId)!!
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

    fun <This, T> loadData(file: File, transform: (File) -> T): DelegatedResult<This, T> {
        require(file.isSubDirectoryOf(dataFolder))
        val pageManager = KNote.NOTEBOOK_MANAGER.getPageManager()!!
        pageManager.watchDataFile(id, file)
        // TODO: add to input files

        return object : DelegatedResult<This, T> {
            override fun getValue(self: This, property: KProperty<*>): T {
                logger.info("loading $file")
                return transform(file)
            }
        }
    }

    fun <This> loadData(file: File) = loadData<This, File>(file) { file ->
        file
    }

    fun markdownText(block: KNTextBuilder.() -> Unit) {
        text = knote.md.markdownText(block = block).toString()
    }

    // TODO: PathWatcher for changes in file dependencies
    // TODO: basic functions to load file contents

    internal fun invalidate() {
    }

    // TODO: visualize data
}