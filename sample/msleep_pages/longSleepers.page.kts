import krangl.DataFrame
import krangl.*

val sleepData: DataFrame = fromPage("sleepData")

fun process(): DataFrame = sleepData.filter { it["sleep_total"] gt 16}