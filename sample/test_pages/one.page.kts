@file:Import("IncludeTest.kt")

logger.info("evaluating $id")
fun process(@FromPage two: List<CharSequence>, @FromPage three: CharSequence): String {
    logger.info("executing $id")
    return "one 1 + $two + $three"
}