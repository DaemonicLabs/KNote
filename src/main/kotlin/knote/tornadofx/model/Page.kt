package knote.tornadofx.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class Page(pageName: String, script: String, results: Any?) {
    val pageNameProperty = SimpleStringProperty(this, "", pageName)
    var name by pageNameProperty

    val scriptProperty = SimpleStringProperty(this, "", script)
    var script by scriptProperty

    val resultsProperty = SimpleObjectProperty(this, "", results)
    val results by resultsProperty
}

class PageModel: ItemViewModel<Page>() {
    val pageName = bind(Page::pageNameProperty)
    val script = bind(Page::scriptProperty)
}

class PageModelScope: Scope() {
    val model = PageModel()
}

