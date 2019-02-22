package knote

import knote.util.watch
import java.io.File

fun main(args : Array<String>) {
    val path = File("pages").toPath()

    val watcher = path.watch()
    println("Press ctrl+c to exit")

    while (true) {
        //The watcher blocks until an event is available
        val key = watcher.take()

        //Now go through each event on the folder
        key.pollEvents().forEach { it ->
            //Print output according to the event
            when (it.kind().name()) {
                "ENTRY_CREATE" -> println("${it.context()} was created")
                "ENTRY_MODIFY" -> println("${it.context()} was modified")
                "OVERFLOW" -> println("${it.context()} overflow")
                "ENTRY_DELETE" -> println("${it.context()} was deleted")
            }
        }
        //Call reset() on the key to watch for future events
        key.reset()
    }
}