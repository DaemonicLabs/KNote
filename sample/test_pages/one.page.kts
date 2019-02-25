//@file:Import("IncludeText.kt")
import knote.annotations.FromPage

logger.info("evaluating $id")
fun process(@FromPage two: String,@FromPage three: String): String {
    logger.info("executing $id")
    return "one + $two + $three"
}