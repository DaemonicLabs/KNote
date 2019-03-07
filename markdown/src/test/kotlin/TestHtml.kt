import knote.md.inlineHtml
import kotlinx.html.BODY
import kotlinx.html.DIV
import kotlinx.html.TagConsumer
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.br
import kotlinx.html.cite
import kotlinx.html.html
import kotlinx.html.stream.createHTML

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