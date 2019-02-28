@file:Import("Item.kt")

fun process(): Sequence<Item> {
    val sequence = sequenceOf(
        Pair("Alpha", 4.0),
        Pair("Beta", 6.0),
        Pair("Gamma", 7.2),
        Pair("Delta", 9.2),
        Pair("Epsilon", 6.8),
        Pair("Zeta", 2.4),
        Pair("Iota", 8.8)
    )
    println(sequence.toList())
    return sequence
}