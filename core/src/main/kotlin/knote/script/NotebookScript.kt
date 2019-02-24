package knote.script

import knote.poet.NotePage
import knote.poet.PageMarker
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "NoteBook",
    fileExtension = "notebook.kts",
    compilationConfiguration = NotebookConfiguration::class
)
open class NotebookScript(val id: String, val rootDir: File) {
    override fun toString() = "NotebookScript(id=$id)"
    var title: String = ""
    var description: String = ""

    var pageRoot: File = rootDir.resolve("${id}_pages")

    val pageFiles: Array<out File>
        get() = pageRoot.listFiles { file -> file.isFile && file.name.endsWith(".page.kts")}

    fun fileForPage(pageId: String): File? {
        val file = pageRoot.resolve("$pageId.page.kts")
        if(file in pageFiles) return file
        println("file $file does not belong to notebook $id")
        return null
    }

    @Deprecated("use includedPageFiles")
    val includes: List<NotePage> = pageFiles.map {
        val id = it.name.substringBeforeLast(".page.kts")
        NotePage(id, it)
    }
    // TODO: add notebook title, descriptions, meta info
}