@file:Import("Item.kt")
import org.nield.kotlinstatistics.sumBy

val sequence: Sequence<Item> = fromPage("sequence")!!
//val sequence = sequenceOf(
//        Item("Alpha", 4.0),
//        Item("Beta", 6.0),
//        Item("Gamma", 7.2),
//        Item("Delta", 9.2),
//        Item("Epsilon", 6.8),
//        Item("Zeta", 2.4),
//        Item("Iota", 8.8)
//    )
fun process(): Map<Int, Double>? {
    println("sequence: ${sequence.toList()}")
    return sequence?.sumBy<Item, Int>(keySelector = { it.name.length }, doubleSelector = { it.value } )
        ?: mapOf()
}