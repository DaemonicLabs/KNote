import mu.KLogging
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TestAnnotation(val source: String = "")

class AnnotationTest() {
    fun process(@TestAnnotation data: String) {
    }

    companion object : KLogging() {
        @JvmStatic
        fun main(args: Array<String>) {
            val processFun = AnnotationTest::class.functions.find { it.name == "process" }!!
            logger.info("process: $processFun")
            logger.info("process.parameters: ${processFun.parameters}")
            processFun.parameters.forEach { parameter ->
                logger.info("parameter: $parameter")
                logger.info("parameter.index: ${parameter.index}")
                logger.info("parameter.name: ${parameter.name}")
                logger.info("parameter.type: ${parameter.type}")
                logger.info("parameter.annotations: ${parameter.annotations}")
                val annotation = parameter.findAnnotation<TestAnnotation>()
            }
        }
    }
}