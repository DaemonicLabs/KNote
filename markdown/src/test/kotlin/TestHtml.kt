import knote.md.inlineHtml
import kotlinx.html.a
import kotlinx.html.br
import kotlinx.html.cite

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