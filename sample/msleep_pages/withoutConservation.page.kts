//@file:FromPage("sleepData")
import krangl.DataFrame

val sleepData: DataFrame by inject()

fun process() = sleepData.remove("conservation")