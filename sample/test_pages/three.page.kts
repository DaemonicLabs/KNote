import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.html.html

logger.info("evaluating $id")
fun process(@FromPage two: List<String>): String {
    logger.info("executing $id")
    return createHTML().html {
        body {
            +"three"
        }
    }.replace("\n", "\\n")
}