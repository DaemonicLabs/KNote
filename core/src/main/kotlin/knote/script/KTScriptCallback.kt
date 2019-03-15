package knote.script

import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtScript
import kotlin.script.experimental.util.PropertiesCollection

object KTScriptCallback {
//    val ktScriptCallbackHandler by PropertiesCollection.key<(KtScript?) -> Unit>()
    val ktScriptCallbackHandler by PropertiesCollection.key<(KtFile?) -> Unit>()
}