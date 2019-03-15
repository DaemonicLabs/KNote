package knote.tornadofx.controller

import mu.KLogging
import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import tornadofx.Controller
import java.util.Collections
import java.util.Random
import java.util.regex.Pattern

class NotebookSpaceController: Controller() {

    private val view: NotebookSpace by inject()

    fun parseAST(textFile: String) {
        // val file = Parser.parseFile(textFile, true)
    }

    fun computeHighlightingAsync(): Task<StyleSpans<Collection<String>>> {
        val text = view.codeArea.text
        val task = object : Task<StyleSpans<Collection<String>>>() {
            @Throws(Exception::class)
            override fun call(): StyleSpans<Collection<String>> {
                return computeHighlighting(text)
            }
            logger.info("[$index] element: $element ${element::class}")
            logger.info(element.text)
//            index += element.
            spansBuilder.add(Collections.emptyList(),  element.startOffset - lastKwEnd)
            //TODO: switch on element type and use the appropriate style classes here
            val styleClass = when(Random().nextInt(3)) {
                0 -> "keyword"
                1 -> "string"
                2 -> "comment"
                else -> ""
            }
            spansBuilder.add(Collections.singleton(styleClass), element.endOffset - element.startOffset)
            index = element.endOffset
            lastKwEnd = element.endOffset
        }
        return spansBuilder.create()
    }

//    private fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
//        val matcher = PATTERN.matcher(text)
//        var lastKwEnd = 0
//        val spansBuilder = StyleSpansBuilder<Collection<String>>()
//        while (matcher.find()) {
//            val styleClass = (when {
//                matcher.group("KEYWORD") != null -> "keyword"
//                matcher.group("PAREN") != null -> "paren"
//                matcher.group("BRACE") != null -> "brace"
//                matcher.group("BRACKET") != null -> "bracket"
//                matcher.group("SEMICOLON") != null -> "semicolon"
//                matcher.group("STRING") != null -> "string"
//                matcher.group("COMMENT") != null -> "comment"
//                else -> ""
//            })
//            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
//            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start())
//            lastKwEnd = matcher.end()
//        }
//        spansBuilder.add(listOf(), text.length - lastKwEnd)
//        return spansBuilder.create()
//    }

    companion object : KLogging() {
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