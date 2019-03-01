[![Build Status](https://jenkins.modmuss50.me/buildStatus/icon?job=NikkyAI/DaemonicLabs/KNote/master)](https://jenkins.modmuss50.me/job/NikkyAI/job/DaemonicLabs/job/KNote/job/master/)  
[![maven-badge](https://img.shields.io/maven-metadata/v/https/maven.modmuss50.me/daemoniclabs/knote/daemoniclabs.knote.gradle.plugin/maven-metadata.xml.svg?style=flat-square&logo=Kotlin)](https://maven.modmuss50.me/daemoniclabs/knote)
# KNote
## Setup Instructions

Before opening the Project in Intellij Idea run `./gradlew publishToMavenLocal` to save yourself a restart later

For Unix (linux / osx)
1. Select the default gradle wrapper distribution.

![alttext](https://i.imgur.com/Nr2sfcX.png)

2. You will also need to import sample/build.script.kts

![alttext](https://i.imgur.com/gqpjGvW.png)
![alttext](https://i.imgur.com/YiFCupt.png)

3. Yours might look red and angry and missing your Run Configurations.
Open the gradle sidebar for `sample-notebook > tasks > application`.
When that's done running, you may need to restart your IDEA, as it's necessary for any large change in script definitions.

![alttext](https://i.imgur.com/G5bNK7R.png)

## Docs

### Notebook

notebooks files have to match `notebooks/*.notebook.kts`

in a `.notebook.kts` you can set the title and description of a notebook

and register type adapters for visualization (WIP)

**TODO**: add defaultImports  
**TODO**: add result adapter

### Page

page files have to match `${notebookId}_pages/*.page.kts`

Pages can also have customizable titles and descriptions (WIP)
as well as process data

example using krangle
```kotlin
import krangl.DataFrame
import krangl.readCSV

fun process(): DataFrame {
    val csvFile = rootDir.resolve("data").resolve("msleep.csv")
    val sleepData = DataFrame.readCSV(csvFile)
    return sleepData
}
```

NOTE: possibly process function will be replaced by `val result get() = ...`

Pages can also use the results from other pages (in the same notebook)

```kotlin
@file:FromPage("sleepData")
import krangl.*

fun process(): DataFrame = sleepData.filter { it["sleep_total"] gt 16}
```

