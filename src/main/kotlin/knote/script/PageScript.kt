package knote.script

import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "Notebook Page",
    fileExtension = "page.kts",
    compilationConfiguration = PageConfiguration::class
)
open class PageScript(val id: String?) {
    override fun toString() = "PageScript(id=$id)"

    open fun process() : Any? {
        return null
    }

    // TODO: process data

    // TODO: visualize data
}