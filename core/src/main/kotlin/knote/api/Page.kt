package knote.api

import knote.script.PageScript
import knote.util.KObservableList
import knote.util.KObservableObject
import knote.util.MutableKObservableObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtScript
import java.io.File
import java.nio.file.Path
import kotlin.script.experimental.api.ScriptDiagnostic

interface Page {
    val id: String
    val fileObject: KObservableObject<Page, File>
    val fileContentObject: KObservableObject<Page, String>
    val textObject: KObservableObject<Page, String>
    val compiledScriptObject: KObservableObject<Page, PageScript?>
    val reportsObject: KObservableObject<Page, List<ScriptDiagnostic>?>
    val resultObject: KObservableObject<Page, Any?>
    val dependenciesObject: KObservableObject<Page, Set<String>>
    val fileInputs: KObservableList<Path>
//    val ktScriptObject: KObservableObject<Page, KtScript?>
    val ktScriptObject: KObservableObject<Page, KtFile?>

    val file get() = fileObject.value
    val fileContent get() = fileContentObject.value
    val text get() = textObject.value
    val compiledScript get() = compiledScriptObject.value
    val reports get() = reportsObject.value
    val result get() = resultObject.value
    val dependencies get() = dependenciesObject.value
    val ktScript get() = ktScriptObject.value
}