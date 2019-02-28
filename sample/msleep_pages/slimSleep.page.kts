import krangl.DataFrame

val sleepData = fromPage<DataFrame>("sleepData")

fun process(): DataFrame {
    val slimSleep = sleepData.select("name", "sleep_total")
    return slimSleep
}