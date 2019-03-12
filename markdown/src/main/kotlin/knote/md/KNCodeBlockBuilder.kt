package knote.md

import net.steppschuh.markdowngenerator.text.code.CodeBlock
import net.steppschuh.markdowngenerator.text.code.CodeBlockBuilder

class KNCodeBlockBuilder(
    override val parent: CodeBlockBuilder
) : KNMarkdownBuilder<CodeBlockBuilder, CodeBlock>(parent) {

}