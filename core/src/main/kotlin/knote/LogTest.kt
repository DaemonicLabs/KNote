package knote

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KLogging

object LogTest : KLogging() {
    @JvmStatic
    fun main(args : Array<String>) {
        runBlocking {
            delay(10)
            logger.info("test")
        }
    }
}