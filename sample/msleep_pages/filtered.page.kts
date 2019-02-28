import krangl.DataFrame
import krangl.*

//val longSleepers: DataFrame = fromPage("longSleepers")
val sleepData: DataFrame by inject()
val longSleepers: DataFrame by inject()

fun process(): DataFrame {
    return longSleepers.select({listOf("name")}, { startsWith("sl")})
}