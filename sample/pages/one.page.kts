println("constructing one.page.kts")
fun process(@FromPage two: String): String {
    return "one + $two"
}