@file:FromPage("two")
@file:FromPage("three")

logger.info(">>>> evaluating step $id")

fun process(): String {
    logger.info(">>>> executing step $id")
    return "one, two=$two, three=$three"
}

markdownText {
    +"first page"
}