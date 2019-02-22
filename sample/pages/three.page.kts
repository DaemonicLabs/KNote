import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.html.html

fun process(): Any? {
    return createHTML().html {
        body {
            +"two"
        }
    }.replace("\n", "\\n")
}