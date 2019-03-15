package knote.data

import knote.api.Page
import knote.script.PageScript
import knote.util.MutableKObservableList
import knote.util.MutableKObservableObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtScript
import java.io.File
import java.nio.file.Path
import kotlin.script.experimental.api.ScriptDiagnostic

class PageImpl(
    override val id: String,
    file: File,
    fileContent: String = file.readText(),
    text: String = "",
    compiledScript: PageScript? = null,
    reports: List<ScriptDiagnostic>? = null
) : Page {
    override val textObject: MutableKObservableObject<Page, String> = MutableKObservableObject(text)
    override val fileObject: MutableKObservableObject<Page, File> = MutableKObservableObject(file)
    override val fileContentObject: MutableKObservableObject<Page, String> = MutableKObservableObject(fileContent)
    override val compiledScriptObject: MutableKObservableObject<Page, PageScript?> =
        MutableKObservableObject(compiledScript)
    override val reportsObject: MutableKObservableObject<Page, List<ScriptDiagnostic>?> =
        MutableKObservableObject(reports)
    override val resultObject: MutableKObservableObject<Page, Any?> = MutableKObservableObject(null)
    override val dependenciesObject: MutableKObservableObject<Page, Set<String>> =
        MutableKObservableObject(setOf())
    override val fileInputs: MutableKObservableList<Path> = MutableKObservableList()
//    override val ktScriptObject: MutableKObservableObject<Page, KtScript?> = MutableKObservableObject(null)
    override val ktScriptObject: MutableKObservableObject<Page, KtFile?> = MutableKObservableObject(null)

    override var text by textObject
    override var file by fileObject
    override var fileContent by fileContentObject
    override var compiledScript by compiledScriptObject
    override var reports by reportsObject
    override var result by resultObject
    override var dependencies by dependenciesObject
    override var ktScript by ktScriptObject
    var errored: Boolean = false
}