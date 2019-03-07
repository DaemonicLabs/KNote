package knote.script

import mu.KLogger
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    displayName = "NoteBook",
    fileExtension = "notebook.kts",
    compilationConfiguration = NotebookConfiguration::class
)
open class NotebookScript(
    val id: String,
    val notebookDir: File
) {
    var title: String = ""
    var description: String = ""

    override fun toString() = "NotebookScript(id=$id, title=$title, description=$description)"

    var pageRoot: File = notebookDir.resolve("pages")

    val pageFiles: Array<out File>
        get() = pageRoot.listFiles { file -> file.isFile && file.name.endsWith(".page.kts") }

    fun fileForPage(pageId: String, logger: KLogger): File? {
        val file = pageRoot.resolve("$pageId.page.kts")
        if (file !in pageFiles) {
            logger.error("file $file does not belong to notebook $id")
            return null
        }
        return file
    }

    // TODO: add notebook title, descriptions, meta info
}