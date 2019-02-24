package knote.tornadofx.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import knote.api.PageRegistry
import tornadofx.*
import java.io.File

class Page(file: File, pageName: String, script: String, results: String? = null) {
    val fileProperty = SimpleObjectProperty(this, "", file)
    var file by fileProperty

    val pageNameProperty = SimpleStringProperty(this, "", pageName)
    var pageName by pageNameProperty

    val scriptProperty = SimpleStringProperty(this, "", script)
    var script by scriptProperty

    val resultsProperty = SimpleStringProperty(this, "", results)
    val results by resultsProperty
}

class PageRegistryScope(val registry: PageRegistry, val pages: List<Page>): Scope()

