package knote.tornadofx.controller

import javafx.scene.text.Text
import knote.tornadofx.model.PageManagerEvent
import knote.tornadofx.view.Workbench
import tornadofx.*

class WorkbenchController: Controller() {

    private val view: Workbench by inject()

    init {
        subscribe<PageManagerEvent> { event ->
            updateEvaluationConsole(event.eval.toString())
        }
    }

    fun updateEvaluationConsole(results: String) {
        view.evaluationConsole.children.clear()
        view.evaluationConsole.children.add(Text(results))
        view.rerunButton.isDisable = true
    }
}