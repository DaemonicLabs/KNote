//@file:FromPage("sleepData")
import krangl.*

val sleepData: DataFrame by inject()

fun process() = sleepData.select { range("name", "order") }