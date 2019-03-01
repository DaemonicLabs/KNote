import krangl.DataFrame
import krangl.*

//val longSleepers: DataFrame = fromPage("longSleepers")
val longSleepers: DataFrame by inject()
val sleepData: DataFrame by inject()

fun process(): DataFrame {
    return longSleepers.select({listOf("name")}, { startsWith("sl")})
}