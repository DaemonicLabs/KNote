import kotlinx.html.stream.createHTML
import kotlinx.html.html

fun process(): Any? {
    return createHTML().html {
        +"two"
    }.replace("\n", "\\n")
}