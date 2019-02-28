import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.createHTML

logger.info(">>>> evaluating step $id")
fun process(
    @FromPage two: String
): String {
    logger.info(">>>> executing step $id")

    return createHTML().html {
        body {
            +"three"
        }
    }.replace("\n", "\\n")
}