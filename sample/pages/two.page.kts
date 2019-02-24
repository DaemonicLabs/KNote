import kotlinx.html.stream.createHTML
import kotlinx.html.html

fun process(): Any? {
    return createHTML().html {
        +"two 2"
    }.replace("\n", "\\n")
}