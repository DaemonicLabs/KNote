package knote.api

import knote.script.NotebookScript
import knote.util.KObservableObject
import java.io.File
import kotlin.script.experimental.api.ScriptDiagnostic

interface Notebook {
    val id: String
    val fileObject: KObservableObject<Notebook, File>
    val fileContentObject: KObservableObject<Notebook, String>
    val compiledScriptObject: KObservableObject<Notebook, NotebookScript?>
    val reportsObject: KObservableObject<Notebook, List<ScriptDiagnostic>?>
    val pageManagerObject: KObservableObject<Notebook, PageManager?>

    val file get() = fileObject.value
    val fileContent get() = fileContentObject.value
    val compiledScript get() = compiledScriptObject.value
    val reports get() = reportsObject.value
    val pageManager get() = pageManagerObject.value
}
