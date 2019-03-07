package knote.md

import kotlinx.html.BODY
import kotlinx.html.body
import kotlinx.html.html
import kotlinx.html.stream.createHTML
import net.steppschuh.markdowngenerator.MarkdownBuilder
import net.steppschuh.markdowngenerator.text.Text
import net.steppschuh.markdowngenerator.text.TextBuilder

fun markdownText(
    parent: MarkdownBuilder<TextBuilder, Text>? = null,
    block: KNTextBuilder.() -> Unit
): Text {
    val builder = KNTextBuilder(TextBuilder(parent))
    builder.block()
    return builder.build()
}

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