import kotlinx.html.stream.createHTML
import kotlinx.html.html

logger.info("evaluating $id")
fun process(): List<String> {
    logger.info("executing $id")
    return listOf(createHTML().html {
        +"two 2 222"
    }.replace("\n", "\\n"))
}