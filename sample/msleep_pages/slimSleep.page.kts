@file:FromPage("sleepData")
import krangl.DataFrame

fun process(): DataFrame {
    val slimSleep = sleepData.select("name", "sleep_total")
    return slimSleep
}