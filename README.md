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

