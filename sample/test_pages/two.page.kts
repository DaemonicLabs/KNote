import kotlinx.html.html
import kotlinx.html.stream.createHTML

logger.info(">>>> evaluating step $id")
fun process(): String {
    logger.info(">>>> executing step $id")
    return createHTML().html {
        +"two 2"
    }.replace("\n", "\\n")
}