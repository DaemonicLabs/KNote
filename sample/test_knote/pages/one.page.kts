//@file:FromPage("two")
//@file:FromPage("three")

val two: String by inject()
val three: String by inject()

logger.info(">>>> evaluating step $id")

fun process(): String {
    logger.info(">>>> executing step $id")
    return "one, two=$two, three=$three"
}

markdownText {
    +"first page"
}