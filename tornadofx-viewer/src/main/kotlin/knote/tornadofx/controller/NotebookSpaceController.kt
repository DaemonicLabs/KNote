package knote.tornadofx.controller

import javafx.scene.text.Text
import knote.tornadofx.view.NotebookSpace
import tornadofx.*

class NotebookSpaceController: Controller() {

    private val view: NotebookSpace by inject()

    fun updateEvaluationConsole(results: String) {
        view.evaluationConsole.children.clear()
        view.evaluationConsole.children.add(Text(results))
        // view.rerunButton.isDisable = true
    }
}