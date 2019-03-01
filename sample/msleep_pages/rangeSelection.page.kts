import krangl.DataFrame
import krangl.*

val sleepData: DataFrame by inject()

fun process() = sleepData.select { range("name", "order") }