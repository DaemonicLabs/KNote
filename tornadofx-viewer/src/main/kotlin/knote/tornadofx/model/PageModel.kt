package knote.tornadofx.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import knote.api.Notebook
import knote.api.Page
import knote.api.PageManager
import knote.util.KObservableObject
import tornadofx.*
import java.io.File

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

class PageManagerScope(val pageManager: PageManager,
                       val pageManagerObject: KObservableObject<Notebook, PageManager?>,
                       val pageViewModels: List<PageViewModel>): Scope()

class PageManagerChangeListener(val pageName: String, scope: PageManagerScope): FXEvent(EventBus.RunOn.BackgroundThread, scope)

class PageManagerEvent(val eval: Page?) : FXEvent(EventBus.RunOn.BackgroundThread)



