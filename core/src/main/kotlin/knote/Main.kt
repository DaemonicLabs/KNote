package knote

import mu.KLogging

object Main : KLogging() {
    @JvmStatic
    fun main(vararg args: String) {
        KNote.NOTEBOOK_REGISTRY.notebookFilter = args.toList()
        KNote.NOTEBOOK_REGISTRY.evalNotebooks()

        KNote.NOTEBOOK_REGISTRY.compiledNotebooks.forEach { id, notebook ->
            val pageRegistry = KNote.PAGE_REGISTRIES[id]!!
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
