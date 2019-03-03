@file:FromPage("two")

import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.createHTML

fun process(): String {
    logger.info(">>>> executing step $id")

    return createHTML().html {
        body {
            +"three + $two"
        }
    }.replace("\n", "\\n")
}