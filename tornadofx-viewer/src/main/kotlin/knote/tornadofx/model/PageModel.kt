package knote.tornadofx.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import knote.api.PageManager
import tornadofx.*
import java.io.File

class PageModel(file: File, pageName: String, script: String, results: String? = null, dirtyState: Boolean = false) {
    val fileProperty = SimpleObjectProperty(this, "", file)
    var file by fileProperty

    val pageNameProperty = SimpleStringProperty(this, "", pageName)
    var pageName by pageNameProperty

    val scriptProperty = SimpleStringProperty(this, "", script)
    var script by scriptProperty

    val resultsProperty = SimpleStringProperty(this, "", results)
    var results by resultsProperty

    val dirtyStateProperty = SimpleBooleanProperty(this, "", dirtyState)
    var dirtyState by dirtyStateProperty
}

class PageRegistryScope(val manager: PageManager, val pages: List<PageModel>) : Scope()
