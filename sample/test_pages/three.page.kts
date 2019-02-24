import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.html.html

fun process(@FromPage two: String): Any? {
    return createHTML().html {
        body {
            +"three"
        }
    }.replace("\n", "\\n")
}