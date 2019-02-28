package knote.tornadofx.knote.conversion.layer

import knote.KNote
import knote.tornadofx.model.PageModel
import java.io.File

class ConversionLayer {
    fun KNote.addNewPageToNoteBookFromGUI(notebookId: String, page: PageModel) {
        val notebook = KNote.NOTEBOOK_MANAGER.findNotebook(notebookId)
        val file = File("${page.pageName}.page.kts")

        file.printWriter().use { out -> out.println(page.script) }
//        val notePage = NotePage(file = file, id = page.pageName)
        // add to notebook, then execute script to generate Pages and also add to the notebook
    }

    fun KNote.sendToKNote(notebookId: String, page: PageModel) {
    }
}