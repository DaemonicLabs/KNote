//@file:FromPage("sequence")
import org.nield.kotlinstatistics.sumBy

val sequence: Sequence<Item> by inject()

fun process(): Map<Int, Double>? {
    println("sequence: ${sequence.toList()}")
    return sequence.sumBy<Item, Int>(keySelector = { it.name.length }, doubleSelector = { it.value } )
        ?: mapOf()
}