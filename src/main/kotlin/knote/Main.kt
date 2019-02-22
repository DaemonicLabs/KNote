package knote

fun main(vararg args: String) {
    // TODO: start tornadofx application
    // TODO: file change listener

    KNote.notebookFilter = args.toList()
    KNote.evalNotebooks()

    KNote.notebooks.forEach { notebook ->
        val pageRegistry = KNote.pageRegistries[notebook.id]!!
        pageRegistry.allResults.forEach { pageId, result ->
            println("[$pageId]: KClass: ${result::class} value: '$result'")
        }
    }

    while(true) {
        Thread.sleep(1000)
    }
//    KNote.shutdown()
}