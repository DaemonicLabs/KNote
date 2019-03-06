package knote.tornadofx.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import knote.api.Notebook
import knote.api.Page
import knote.api.PageManager
import knote.util.KObservableObject
import tornadofx.*
import java.io.File

class NotebookModel(notebook: Notebook, pageManager: PageManager, pageViewModels: List<PageViewModel>) {
    val notebookProperty = SimpleObjectProperty(this, "", notebook)
    var notebook by notebookProperty

    val pageManagerProperty = SimpleObjectProperty(this, "", pageManager)
    var pageManager by pageManagerProperty

    val pageViewModelsProperty = SimpleListProperty(this, "", pageViewModels.observable())
    var pageViewModels by pageViewModelsProperty

}

class NotebookViewModel(notebook: NotebookModel): ItemViewModel<NotebookModel>() {
    val notebook = bind(NotebookModel::notebookProperty, autocommit = true)
    val pageManager = bind(NotebookModel::pageManagerProperty, autocommit = true)
    val pageViewModels = bind(NotebookModel::pageViewModelsProperty, autocommit = true)
}

class PageViewModel(file: File, pageId: String, script: String, results: String? = null, dirtyState: Boolean = false) {
    val fileProperty = SimpleObjectProperty(this, "", file)
    var file by fileProperty

    val pageIdProperty = SimpleStringProperty(this, "", pageId)
    var pageId by pageIdProperty

    val scriptProperty = SimpleStringProperty(this, "", script)
    var script by scriptProperty

    val resultsProperty = SimpleStringProperty(this, "", results)
    var results by resultsProperty

    val dirtyStateProperty = SimpleBooleanProperty(this, "", dirtyState)
    var dirtyState by dirtyStateProperty
}

class NotebookScope(val notebook: Notebook,
                    val pageManager: PageManager,
                    val pageViewModels: List<PageViewModel>): Scope()

class PageManagerScope(val pageManager: PageManager,
                       val pageViewModels: List<PageViewModel>): Scope()



