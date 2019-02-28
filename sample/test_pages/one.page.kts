logger.info(">>>> evaluating step $id")
fun process(
    @FromPage two: String,
    @FromPage three: CharSequence
): String {
    logger.info(">>>> executing step $id")
    return "one, two=$two, three=$three"
}