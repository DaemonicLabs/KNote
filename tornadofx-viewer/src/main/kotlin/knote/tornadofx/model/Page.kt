package knote.tornadofx.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class Page(pageName: String, script: String, results: String? = null) {
    val pageNameProperty = SimpleStringProperty(this, "", pageName)
    var pageName by pageNameProperty

    val scriptProperty = SimpleStringProperty(this, "", script)
    var script by scriptProperty

    val resultsProperty = SimpleStringProperty(this, "", results)
    val results by resultsProperty
}

class PageViewModel: ItemViewModel<Page>() {
    val pageName = bind(Page::pageNameProperty)
    val script = bind(Page::scriptProperty)
    val results = bind(Page::resultsProperty)
}

