import Item

fun process(): Sequence<Item> {
    val sequence = sequenceOf(
        Item("Alpha", 4.0),
        Item("Beta", 6.0),
        Item("Gamma", 7.2),
        Item("Delta", 9.2),
        Item("Epsilon", 6.8),
        Item("Zeta", 2.4),
        Item("Iota", 8.8)
    )
    println(sequence.toList())
    return sequence
}