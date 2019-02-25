package knote.script

import mu.KLogging
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "Notebook Page",
    fileExtension = "page.kts",
    compilationConfiguration = PageConfiguration::class
)
open class PageScript(val id: String) {
    companion object : KLogging()
    override fun toString() = "PageScript(id=$id)"

    open fun process() : Any? {
        return null
    }

    // TODO: process data

    // TODO: visualize data
}