//@file:FromPage("sleepData")
//@file:FromPage("longSleepers")
import krangl.*

val sleepData: DataFrmae by inject()
val longSleepers: DataFrmae by inject()

fun process(): DataFrame {
    return longSleepers.select({listOf("name")}, { startsWith("sl")})
}