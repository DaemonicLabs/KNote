//@file:FromPage("sleepData")
import krangl.DataFrame

val sleepData: DataFrmae by inject()

fun process(): DataFrame {
    val slimSleep = sleepData.select("name", "sleep_total")
    return slimSleep
}