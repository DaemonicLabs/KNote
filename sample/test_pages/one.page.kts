//@file:Import("IncludeText.kt")
import knote.annotations.FromPage

logger.info("evaluating $id")
fun process(@FromPage two: List<CharSequence>, @FromPage three: CharSequence): String {
    logger.info("executing $id")
    return "one + $two + $three"
}