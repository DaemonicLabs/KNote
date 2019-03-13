package knote.tornadofx

import javafx.scene.paint.Color
import tornadofx.*

class Styles: Stylesheet() {
    companion object {
        val evaluationConsole by cssclass()
    }

    init {
        evaluationConsole {
            backgroundColor += Color.WHITE
            padding = box(10.px)
        }
    }
}