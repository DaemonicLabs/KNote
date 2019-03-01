@file:FromPage("sleepData")
import krangl.*

fun process(): DataFrame = sleepData.filter { it["sleep_total"] gt 16}