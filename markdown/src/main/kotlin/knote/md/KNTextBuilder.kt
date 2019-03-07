package knote.md

import net.steppschuh.markdowngenerator.text.Text
import net.steppschuh.markdowngenerator.text.TextBuilder

class KNTextBuilder (
    override val parent: TextBuilder
): KNMarkdownBuilder<TextBuilder, Text>(parent)