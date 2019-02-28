package knote

import mu.KLogging

object Main : KLogging() {
    @JvmStatic
    fun main(vararg args: String) {
        KNote.NOTEBOOK_MANAGER.notebookFilter = args.toList()
        KNote.NOTEBOOK_MANAGER.evalNotebooks()

        KNote.NOTEBOOK_MANAGER.notebooks.forEach { (id, notebook) ->
            notebook.pageManager!!.executeAll().forEach { pageId, result ->
                logger.info("[$pageId]: KClass: ${result::class} value: '$result'")
            }
        }

        while (true) {
            Thread.sleep(1000)
        }
//    KNote.shutdown()
    }
}
