package knote.util

import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import org.fxmisc.richtext.CodeArea
import tornadofx.*

fun EventTarget.codearea(text: String, op: CodeArea.() -> Unit = {}): CodeArea {
    val codeArea = CodeArea(text)
    return opcr(this, codeArea, op)
}