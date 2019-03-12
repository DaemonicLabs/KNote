//@file:FromPage("two")

import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.createHTML

val two: String by inject()

fun process(): String {
    logger.info(">>>> executing step $id")

    return createHTML().html {
        body {
            +"three + $two"
        }
    }.replace("\n", "\\n")
}