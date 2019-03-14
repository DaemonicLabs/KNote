import krangl.DataFrame
import krangl.readCSV
val sleepData by loadData(dataFolder.resolve("msleep.csv")) { file ->
    DataFrame.readCSV(file)
}
fun process(): DataFrame {
    return sleepData
}