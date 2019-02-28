import knote.util.watch
import mu.KLogging
import java.io.File

object WatchTest : KLogging() {
    @JvmStatic
    fun main(args: Array<String>) {
        val path = File("pages").toPath()

        val watcher = path.watch()
        logger.info("Press ctrl+c to exit")

        while (true) {
            //The watcher blocks until an event is available
            val key = watcher.take()

            //Now go through each event on the folder
            key.pollEvents().forEach { it ->
                //Print output according to the event
                when (it.kind().name()) {
                    "ENTRY_CREATE" -> logger.info("${it.context()} was created")
                    "ENTRY_MODIFY" -> logger.info("${it.context()} was modified")
                    "OVERFLOW" -> logger.info("${it.context()} overflow")
                    "ENTRY_DELETE" -> logger.info("${it.context()} was deleted")
                }
            }
            //Call reset() on the key to watch for future events
            key.reset()
        }
    }
}