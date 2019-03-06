package knote.md

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