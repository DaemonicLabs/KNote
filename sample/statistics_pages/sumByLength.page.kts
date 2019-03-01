@file:FromPage("sequence")
import org.nield.kotlinstatistics.sumBy

fun process(): Map<Int, Double>? {
    println("sequence: ${sequence.toList()}")
    return sequence.sumBy<Item, Int>(keySelector = { it.name.length }, doubleSelector = { it.value } )
        ?: mapOf()
}