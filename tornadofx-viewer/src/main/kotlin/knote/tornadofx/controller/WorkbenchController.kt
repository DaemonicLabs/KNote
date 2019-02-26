package knote.tornadofx.controller

import javafx.scene.text.Text
import knote.tornadofx.view.Workbench
import tornadofx.*

class WorkbenchController: Controller() {

    private val view: Workbench by inject()

    fun updateEvaluationConsole(results: String) {
        view.evaluationConsole.children.clear()
        view.evaluationConsole.children.add(Text(results))
    }
}