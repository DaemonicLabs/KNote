package knote.md

import net.steppschuh.markdowngenerator.text.Text
import net.steppschuh.markdowngenerator.text.TextBuilder
import net.steppschuh.markdowngenerator.text.code.CodeBlock
import net.steppschuh.markdowngenerator.text.code.CodeBlockBuilder
import net.steppschuh.markdowngenerator.text.quote.Quote
import net.steppschuh.markdowngenerator.text.quote.QuoteBuilder

class KNCodeBlockBuilder (
    override val parent: CodeBlockBuilder
) : KNMarkdownBuilder<CodeBlockBuilder, CodeBlock>(parent) {

}