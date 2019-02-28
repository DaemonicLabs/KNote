import krangl.DataFrame
import krangl.*

val sleepData: DataFrame = fromPage("sleepData")

fun process() = sleepData.select { range("name", "order") }