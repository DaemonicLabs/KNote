package knote.tornadofx

import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

object Styles: Stylesheet() {

    val evaluationConsole by cssclass()

    init {
        evaluationConsole {
            backgroundColor += Color.WHITE
            padding = box(10.px)
        }

    }
}