//@file:FromPage("sleepData")
//@file:FromPage("longSleepers")
import krangl.*

val sleepData: DataFrame by inject()
val longSleepers: DataFrame by inject()

fun process(): DataFrame {
    return longSleepers.select({listOf("name")}, { startsWith("sl")})
}