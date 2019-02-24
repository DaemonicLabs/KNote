package knote

fun main(vararg args: String) {
    // TODO: start tornadofx application
    // TODO: file change listener

    KNote.notebookRegistry.notebookFilter = args.toList()
    KNote.notebookRegistry.evalNotebooks()

    KNote.notebookRegistry.compiledNotebooks.forEach { id, notebook ->
        val pageRegistry = KNote.pageRegistries[id]!!
        pageRegistry.allResults.forEach { pageId, result ->
            println("[$pageId]: KClass: ${result::class} value: '$result'")
        }
    }

    while(true) {
        Thread.sleep(1000)
    }
//    KNote.shutdown()
}