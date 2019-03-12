import knote.md.inlineHtml
import knote.md.markdownText
import kotlinx.html.img
import net.steppschuh.markdowngenerator.list.TaskListItem

fun main() {
    val text = markdownText {
        codeBlock(
            "kotlin",
            """
                import blah
                fun main() = println("Hello World")
                """.trimIndent()
        )
        code("./gradlew publishToMavenLocal")
        newLine()
        newLine()
        list {
            +"element 1"
            +"element 2"
        }
        newLine()
        taskList(
            TaskListItem("task", true),
            TaskListItem("next", false),
            TaskListItem("last")
        )

        +inlineHtml {
            img(src = "something.png")
        }
    }
    println(text)
}