@file:FromPage("sleepData")
import krangl.*

fun process() = sleepData.select { range("name", "order") }