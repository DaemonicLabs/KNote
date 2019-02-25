import kotlinx.html.stream.createHTML
import kotlinx.html.html

logger.info("evaluating $id")
fun process(): Any? {
    logger.info("executing $id")
    return createHTML().html {
        +"two 2"
    }.replace("\n", "\\n")
}