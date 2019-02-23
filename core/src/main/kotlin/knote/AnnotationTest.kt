package knote

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TestAnnotation(val source: String = "")

class AnnotationTest() {
    fun process(@TestAnnotation data: String){

    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val processFun = AnnotationTest::class.functions.find { it.name == "process" }!!
            println("process: $processFun")
            println("process.parameters: ${processFun.parameters}")
            processFun.parameters.forEach { parameter ->
                println("parameter: $parameter")
                println("parameter.index: ${parameter.index}")
                println("parameter.name: ${parameter.name}")
                println("parameter.type: ${parameter.type}")
                println("parameter.annotations: ${parameter.annotations}")
                val annotation = parameter.findAnnotation<TestAnnotation>()
            }
        }
    }
}