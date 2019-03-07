package knote.md

import net.steppschuh.markdowngenerator.MarkdownBuilder
import net.steppschuh.markdowngenerator.MarkdownElement
import net.steppschuh.markdowngenerator.MarkdownSerializable
import net.steppschuh.markdowngenerator.image.Image
import net.steppschuh.markdowngenerator.link.Link
import net.steppschuh.markdowngenerator.list.TaskList
import net.steppschuh.markdowngenerator.list.TaskListItem
import net.steppschuh.markdowngenerator.list.UnorderedList
import net.steppschuh.markdowngenerator.progress.ProgressBar
import net.steppschuh.markdowngenerator.rule.HorizontalRule
import net.steppschuh.markdowngenerator.text.Text
import net.steppschuh.markdowngenerator.text.code.Code
import net.steppschuh.markdowngenerator.text.code.CodeBlock
import net.steppschuh.markdowngenerator.text.emphasis.BoldText
import net.steppschuh.markdowngenerator.text.emphasis.ItalicText
import net.steppschuh.markdowngenerator.text.emphasis.StrikeThroughText
import net.steppschuh.markdowngenerator.text.heading.Heading
import net.steppschuh.markdowngenerator.text.quote.Quote

open class KNMarkdownBuilder<S : MarkdownBuilder<S, T>?, T : MarkdownElement>(
    /**
     * underlying markdown builder
     */
    open val parent: MarkdownBuilder<S, T>
) {
    /**
     * Attempts to append the specified value to the existing root
     * {@link MarkdownBuilder#markdownElement}.
     *
     * @receiver value to be appended
     */
    open operator fun Any.unaryPlus() {
        parent.append(this)
//        newLine()
    }

    /**
     * Attempts to serialize the specified value to markdown and appends
     * it to the existing root {@link MarkdownBuilder#markdownElement}.
     *
     * @receiver value to be appended
     */
    open operator fun MarkdownSerializable.unaryPlus() {
        parent.append(this)
//        newLine()
    }

    /**
     * Appends a normal {@link Text} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value for the new element
     * @see Text#Text(Object)
     */
    fun text(value: Any) {
        parent.text(value)
    }

    /**
     * Appends a {@link BoldText} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value for the new element
     * @see BoldText#BoldText(Object)
     */
    fun bold(value: Any) {
        parent.bold(value)
    }

    /**
     * Appends an {@link ItalicText} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value for the new element
     * @see ItalicText#ItalicText(Object)
     */
    fun italic(value: Any) {
        parent.italic(value)
    }

    /**
     * Appends a {@link StrikeThroughText} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value for the new element
     * @see StrikeThroughText#StrikeThroughText(Object)
     */
    fun strikeThrough(value: Any) {
        parent.strikeThrough(value)
    }

    /**
     * Appends a {@link Heading} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value for the new element
     * @param level the heading level
     * @see Heading#Heading(Object, int)
     */
    fun heading(value: String, level: Int = 1) {
        parent.heading(value, level)
    }

    /**
     * Appends a {@link Heading} element with level 2 to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value for the new element
     * @see Heading#Heading(Object, int)
     */
    fun subHeading(value: String) {
        parent.heading(value, 2)
    }

    /**
     * Appends a {@link HorizontalRule} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @see HorizontalRule#HorizontalRule()
     */
    fun rule(length: Int = HorizontalRule.MINIMUM_LENGTH) {
        parent.rule(length)
    }

    /**
     * Appends a {@link Link} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param text text for the link
     * @param url  url for the link
     * @see Link#Link(Object, String)
     */
    fun link(url: String, text: String = url) {
        parent.link(text, url)
    }

    /**
     * Appends an {@link Image} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param text text for the image
     * @param url  url to the image
     * @see Image#Image(Object, String)
     */
    fun image(url: String, text: String = url) {
        parent.image(text, url)
    }

    /**
     * Appends a {@link ProgressBar} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param progress progress value ranging from 0 to 1
     * @see ProgressBar#ProgressBar(double)
     */
    fun progress(progress: Double) {
        parent.progress(progress)
    }

    /**
     * Appends a {@link ProgressBar} element with a value label to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param progress progress value ranging from 0 to 1
     * @see ProgressBar#ProgressBar(double)
     */
    fun progressWithLabel(progress: Double) {
        parent.progressWithLabel(progress)
    }

    /**
     * Creates a new {@link KNQuoteBuilder} instance.
     *
     * @param block quote builder
     * @see KNQuoteBuilder#KNQuoteBuilder(QuoteBuilder)
     */
    inline fun quote(block: KNQuoteBuilder.() -> Unit) {
        val builder = parent.beginQuote()
        val knBuilder = KNQuoteBuilder(builder)
        knBuilder.block()
        builder.end()
    }

    /**
     * Appends a {@link Quote} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value for the element
     * @see Quote#Quote(Object)
     */
    fun quote(value: String) {
        parent.quote(value)
    }

    /**
     * Creates a new {@link KNCodeBlockBuilder} instance and sets the language.
     *
     * @param language the code language for syntax highlighting
     * @see KNCodeBlockBuilder#KNCodeBlockBuilder(CodeBlockBuilder)
     */
    inline fun codeBlock(
        language: String = CodeBlock.LANGUAGE_UNKNOWN,
        block: KNCodeBlockBuilder.() -> Unit
    ) {
        val builder = parent.beginCodeBlock(language)
        val knBuilder = KNCodeBlockBuilder(builder)
        knBuilder.block()
        builder.end()
        newLine()
    }

    /**
     * Creates a new {@link CodeBlock} and sets the language.
     *
     * @param language the code language for syntax highlighting
     * @see CodeBlock#CodeBlock(Any, String)
     */
    fun codeBlock(
        language: String = CodeBlock.LANGUAGE_UNKNOWN,
        block: String
    ) {
        +CodeBlock(block, language)
    }

    /**
     * Appends a {@link Code} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value for the new element
     * @see Code#Code(Object)
     */
    fun code(value: Any) {
        parent.code(value)
    }

    /**
     * Creates a new {@link KNListBuilder} instance.
     *
     * @see KNListBuilder#KNListBuilder(ListBuilder)
     */
    inline fun list(block: KNListBuilder.() -> Unit) {
        val builder = parent.beginList()
        val knBuilder = KNListBuilder(builder)
        knBuilder.block()
        builder.end()
        newLine()
    }

    /**
     * Appends a {@link UnorderedList} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param items elements that should be list items
     * @see UnorderedList#UnorderedList(List)
     */
    fun unorderedList(vararg items: Any) {
        parent.unorderedList(*items)
    }

    /**
     * Appends a {@link TaskList} element to the root {@link MarkdownBuilder#markdownElement}.
     *
     * @param items elements that should be task items
     * @see TaskList#TaskList(List)
     */
    fun taskList(vararg items: TaskListItem) {
        parent.taskList(*items)
    }

    /**
     * Attempts to append the specified value to the existing root
     * {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value to be appended
     */
    fun append(value: Any) {
        parent.append(value)
    }

    /**
     * Attempts to serialize the specified value to markdown and appends
     * it to the existing root {@link MarkdownBuilder#markdownElement}.
     *
     * @param value value to be appended
     */
    fun append(value: MarkdownSerializable) {
        parent.append(value)
    }
    /**
     * Appends two new lines to the existing root {@link MarkdownBuilder#markdownElement}.
     */
    fun newParagraph() {
        parent.newParagraph()
    }
    /**
     * Appends a new line to the existing root {@link MarkdownBuilder#markdownElement}.
     */
    fun newLine() {
        parent.newLine()
    }

    /**
     * Returns the root {@link MarkdownBuilder#markdownElement}
     * @return {@link MarkdownBuilder#markdownElement}
     */
    fun build(): T = parent.build()
}