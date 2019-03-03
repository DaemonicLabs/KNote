//@file:FromPage("sleepData")
import krangl.DataFrame

val sleepData: DataFrmae by inject()

fun process() = sleepData.remove("conservation")