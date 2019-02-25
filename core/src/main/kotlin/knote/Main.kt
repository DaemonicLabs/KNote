package knote

import mu.KLogging

object Main : KLogging() {
    @JvmStatic
    fun main(vararg args: String) {
        KNote.notebookRegistry.notebookFilter = args.toList()
        KNote.notebookRegistry.evalNotebooks()

        KNote.notebookRegistry.compiledNotebooks.forEach { id, notebook ->
            val pageRegistry = KNote.pageRegistries[id]!!
            pageRegistry.allResults.forEach { pageId, result ->
                logger.info("[$pageId]: KClass: ${result::class} value: '$result'")
            }
        }

        while(true) {
            Thread.sleep(1000)
        }
//    KNote.shutdown()
    }
}
