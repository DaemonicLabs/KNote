package knote

import mu.KLogging

object Main : KLogging() {
    @JvmStatic
    fun main(vararg args: String) {
        val pageManager = KNote.NOTEBOOK_MANAGER.pageManager
        pageManager.executeAll().forEach { pageId, result ->
            logger.info("[$pageId]: KClass: ${result::class} value: '$result'")
        }

        while (true) {
            Thread.sleep(1000)
        }
//    KNote.shutdown()
    }
}
