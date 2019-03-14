package knote.tornadofx.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import knote.api.Notebook
import knote.api.Page
import knote.api.PageManager
import knote.util.asProperty
import tornadofx.*

class NotebookModel(notebook: Notebook, pageManager: PageManager, pageViewModels: ObservableList<PageViewModel>) {
    val notebookProperty = SimpleObjectProperty(this, "", notebook)
    var notebook by notebookProperty

    val pageManagerProperty = SimpleObjectProperty(this, "", pageManager)
    var pageManager by pageManagerProperty

    val pageViewModelsProperty = SimpleListProperty(this, "", pageViewModels)
    var pageViewModels by pageViewModelsProperty

}

class NotebookViewModel(notebookModel: NotebookModel? = null): ItemViewModel<NotebookModel>() {
    val notebook = bind(NotebookModel::notebookProperty, autocommit = true)
    val pageManager = bind(NotebookModel::pageManagerProperty, autocommit = true)
    val pageViewModels = bind(NotebookModel::pageViewModelsProperty, autocommit = true)
}

class PageViewModel(val page: Page, dirtyState: Boolean = false) {
    val fileProperty = page.fileObject.asProperty
    val file by fileProperty

    // this value cannot change, so why make it observable ?
    val pageId = page.id

    val fileContentProperty = page.fileContentObject.asProperty
    val fileContent by fileContentProperty

    val dirtyStateProperty = SimpleBooleanProperty(this, "", dirtyState)
    var dirtyState by dirtyStateProperty

    val resultProperty = objectBinding(page.resultObject.asProperty) {
        this@PageViewModel.dirtyState = false
        value
    }
    val result by resultProperty
    val resultStringProperty = stringBinding(resultProperty) {
        value.toString()
    }
    val resultString by resultStringProperty

}

class NotebookScope(val notebook: Notebook,
                    val pageManager: PageManager,
                    val pageViewModels: ObservableList<PageViewModel>
): Scope() {
    val model = NotebookViewModel()
}

class PageManagerScope(val pageManager: PageManager,
                       val pageViewModels: List<PageViewModel>): Scope()



