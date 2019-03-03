//@file:FromPage("sleepData")
import krangl.*

val sleepData: DataFrmae by inject()

fun process() = sleepData.select { range("name", "order") }