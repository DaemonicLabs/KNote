import kotlinx.html.html
import kotlinx.html.stream.createHTML

logger.info(">>>> evaluating step $id")
fun process(): String {
    logger.info(">>>> executing step $id")
    return createHTML().html {
        +"three 2"
    }.replace("\n", "\\n")
}
