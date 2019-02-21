package knote.script

import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "NoteBook",
    fileExtension = "notebnook.kts",
    compilationConfiguration = NotebookConfiguration::class
)
open class PageScript(val args: Array<Any>) {
    override fun toString() = "PageScript(args = ${args.joinToString(" ")})"

    var result: Any = Unit

    open fun doThings(id: String) {
        //TODO("please override doThings(id: String)")
        println("default doThings(\"$id\")")
    }

    // TODO: process data

    // TODO: visualize data
}