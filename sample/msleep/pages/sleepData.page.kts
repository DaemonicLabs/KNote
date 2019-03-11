import krangl.DataFrame
import krangl.readCSV
val sleepData by loadData(dataFolder.resolve("msleep.csv")) { file ->
    DataFrame.readCSV(file)
}
fun process(): DataFrame {
//    val csvFile = rootDir.resolve("data").resolve("msleep.csv")
//    val sleepData = DataFrame.readCSV(csvFile)
    return sleepData
}