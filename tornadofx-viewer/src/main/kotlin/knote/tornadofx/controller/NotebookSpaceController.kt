package knote.tornadofx.controller

import tornadofx.*
import java.util.regex.Pattern
import javafx.concurrent.Task
import knote.tornadofx.view.NotebookSpace
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import java.util.*

class NotebookSpaceController: Controller() {

    private val view: NotebookSpace by inject()

    fun computeHighlightingAsync(): Task<StyleSpans<Collection<String>>> {
        val text = view.codeArea.text
        val task = object : Task<StyleSpans<Collection<String>>>() {
            @Throws(Exception::class)
            override fun call(): StyleSpans<Collection<String>> {
                return computeHighlighting(text)
            }
        }
        view.executor.execute(task)
        return task
    }

    fun applyHighlighting(highlighting: StyleSpans<Collection<String>>) {
        view.codeArea.setStyleSpans(0, highlighting)
    }

    private fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
        val matcher = PATTERN.matcher(text)
        var lastKwEnd = 0
        val spansBuilder = StyleSpansBuilder<Collection<String>>()
        while (matcher.find()) {
            val styleClass = (when {
                matcher.group("KEYWORD") != null -> "keyword"
                matcher.group("PAREN") != null -> "paren"
                matcher.group("BRACE") != null -> "brace"
                matcher.group("BRACKET") != null -> "bracket"
                matcher.group("SEMICOLON") != null -> "semicolon"
                matcher.group("STRING") != null -> "string"
                matcher.group("COMMENT") != null -> "comment"
                else -> ""
            })
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start())
            lastKwEnd = matcher.end()
        }
        spansBuilder.add(listOf(), text.length - lastKwEnd)
        return spansBuilder.create()
    }

    companion object {
        private val KEYWORDS = arrayOf(
                "abstract",
                "assert",
                "boolean",
                "break",
                "byte",
                "case",
                "catch",
                "char",
                "class",
                "const",
                "continue",
                "default",
                "do",
                "double",
                "else",
                "enum",
                "extends",
                "final",
                "finally",
                "float",
                "for",
                "goto",
                "if",
                "implements",
                "import",
                "instanceof",
                "int",
                "interface",
                "long",
                "native",
                "new",
                "package",
                "private",
                "protected",
                "public",
                "return",
                "short",
                "static",
                "strictfp",
                "super",
                "switch",
                "synchronized",
                "this",
                "throw",
                "throws",
                "transient",
                "try",
                "void",
                "volatile",
                "while"
        )

        val KEYWORD_PATTERN = "\\b(" + KEYWORDS.joinToString("|") + ")\\b"
        const val PAREN_PATTERN = "\\(|\\)"
        const val BRACE_PATTERN = "\\{|\\}"
        const val BRACKET_PATTERN = "\\[|\\]"
        const val SEMICOLON_PATTERN = "\\;"
        const val STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\""
        const val COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"

        private val PATTERN = Pattern.compile(
                "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                + "|(?<PAREN>" + PAREN_PATTERN + ")"
                + "|(?<BRACE>" + BRACE_PATTERN + ")"
                + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                + "|(?<STRING>" + STRING_PATTERN + ")"
                + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
        )
    }
}