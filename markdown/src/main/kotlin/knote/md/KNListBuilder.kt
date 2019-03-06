package knote.md

import net.steppschuh.markdowngenerator.list.ListBuilder
import net.steppschuh.markdowngenerator.list.UnorderedList

class KNListBuilder (
    override val parent: ListBuilder
): KNMarkdownBuilder<ListBuilder, UnorderedList<Any>>(parent) {
//    override operator fun MarkdownSerializable.unaryPlus() {
//        parent.append(this)
//    }
//    override operator fun Any.unaryPlus() {
//        parent.append(this)
//    }
}