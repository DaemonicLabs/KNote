package knote.md

import net.steppschuh.markdowngenerator.text.Text
import net.steppschuh.markdowngenerator.text.TextBuilder
import net.steppschuh.markdowngenerator.text.quote.Quote
import net.steppschuh.markdowngenerator.text.quote.QuoteBuilder

class KNQuoteBuilder (
    override val parent: QuoteBuilder
) : KNMarkdownBuilder<QuoteBuilder, Quote>(parent)