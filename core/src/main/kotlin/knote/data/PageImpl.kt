package knote.data

import knote.api.Page
import knote.script.PageScript
import knote.util.MutableKObservableObject
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

class PageImpl(
    override val id: String,
    file: File,
    fileContent: String = file.readText(),
    compiledScript: PageScript? = null,
    reports: List<ScriptDiagnostic>? = null
) : Page {
    override val fileObject: MutableKObservableObject<Page, File> = MutableKObservableObject(file)
    override val fileContentObject: MutableKObservableObject<Page, String> = MutableKObservableObject(fileContent)
    override val compiledScriptObject: MutableKObservableObject<Page, PageScript?> =
        MutableKObservableObject(compiledScript)
    override val reportsObject: MutableKObservableObject<Page, List<ScriptDiagnostic>?> =
        MutableKObservableObject(reports)
    override val resultObject: MutableKObservableObject<Page, Any?> = MutableKObservableObject(null)
    override val dependenciesObject: MutableKObservableObject<Page, Set<String>> =
        MutableKObservableObject(mutableSetOf())

    override var file by fileObject
    override var fileContent by fileContentObject
    override var compiledScript by compiledScriptObject
    override var reports by reportsObject
    override var result by resultObject
    override var dependencies by dependenciesObject
    var errored: Boolean = false
}