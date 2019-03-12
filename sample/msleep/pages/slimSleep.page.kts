//@file:FromPage("sleepData")
import krangl.DataFrame

val sleepData: DataFrame by inject()

fun process(): DataFrame {
    val slimSleep = sleepData.select("name", "sleep_total")
    return slimSleep
}