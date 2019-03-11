import krangl.DataFrame
import krangl.filter
import krangl.*

val sleepData: DataFrame by inject()

fun process(): DataFrame = sleepData.filter { it["sleep_total"] gt 16}