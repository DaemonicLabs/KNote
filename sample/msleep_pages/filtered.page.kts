@file:FromPage("sleepData")
@file:FromPage("longSleepers")
import krangl.*

fun process(): DataFrame {
    return longSleepers.select({listOf("name")}, { startsWith("sl")})
}