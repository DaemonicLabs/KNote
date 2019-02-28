package knote.data

import knote.api.Notebook
import knote.api.PageManager
import knote.script.NotebookScript
import knote.util.MutableKObservableObject
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

class NotebookImpl(
    override val id: String,
    file: File,
    fileContent: String = file.readText(),
    compiledScript: NotebookScript? = null,
    reports: List<ScriptDiagnostic>? = null,
    pageManager: PageManager? = null
) : Notebook {
    override val fileObject: MutableKObservableObject<Notebook, File> = MutableKObservableObject(file)
    override val fileContentObject: MutableKObservableObject<Notebook, String> = MutableKObservableObject(fileContent)
    override val compiledScriptObject: MutableKObservableObject<Notebook, NotebookScript?> =
        MutableKObservableObject(compiledScript)
    override val reportsObject: MutableKObservableObject<Notebook, List<ScriptDiagnostic>?> =
        MutableKObservableObject(reports)
    override val pageManagerObject: MutableKObservableObject<Notebook, PageManager?> =
        MutableKObservableObject(pageManager)

    override var file by fileObject
    override var fileContent by fileContentObject
    override var compiledScript by compiledScriptObject
    override var reports by reportsObject
    override var pageManager by pageManagerObject
}