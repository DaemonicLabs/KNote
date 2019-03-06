import knote.md.markdownText
import net.steppschuh.markdowngenerator.list.TaskListItem

fun main() {
    val text = markdownText {
        codeBlock("kotlin",
            """
                import blah
                fun main() = println("Hello World")
                """.trimIndent()
        )
        code("./gradlew publishToMavenLocal")
        newLine()
        list {
            +"element 1"
            +"element 2"
        }
        taskList(
            TaskListItem("")
        )
    }
    println(text)
}