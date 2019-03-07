import krangl.DataFrame
import krangl.readCSV

fun process(): DataFrame {
    val csvFile = rootDir.resolve("data").resolve("msleep.csv")
    val sleepData = DataFrame.readCSV(csvFile)
    return sleepData
}