import kotlinx.html.BODY
import kotlinx.html.DIV
import kotlinx.html.TagConsumer
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.cite
import kotlinx.html.html
import kotlinx.html.stream.createHTML

fun inlineHtml(block: BODY.() -> Unit): String {
    val document = createHTML(prettyPrint = true).html {
        body {
            block()
        }
    }
    val body = document.substringAfter("<html>").substringBeforeLast("</html>")
    val content = body.substringAfter("<body>").substringBeforeLast("</body>")
    return content
}

fun main() {

    val htmlString = inlineHtml {
        +"text"
        br
        a("localhost:80/test") {
            cite {
                +"citation"
            }
        }
        +"normal text"
    }
    println(htmlString)
}