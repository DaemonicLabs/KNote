import knote.util.watchActor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mu.KLogging
import java.io.File
import java.nio.file.Path

object WatchTest : KLogging() {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val path = File("pages").toPath()
        val files = File("build").resolve("test_data").run {
            deleteRecursively()
            mkdirs()
            listOf(resolve("data.txt"), resolve("other.txt"))
        }

        val jobs = files.map { startWatcher(it.parentFile.toPath()) }

        files.forEach {
            it.parentFile.mkdirs()
            it.writeText("${it.name} created")
        }

        delay(1000)

        files.forEach {
            it.parentFile.mkdirs()
            it.writeText("${it.name.toUpperCase()} modified")
        }
        files.forEach {
            it.parentFile.mkdirs()
            it.writeText("${it.name.toUpperCase()} modified again")
        }
        delay(1000)
        (0..50).forEach {
            delay(200)
        }
    }

    private fun startWatcher(target: Path): Job {
        logger.debug("starting watcher for $target")
//        watchJob?.cancel()
        val watchJob = watchActor(target) {
            var timeout: Job? = null
            for (watchEvent in channel) {
                val path = watchEvent.context()
                val file = target.toFile().resolve(path.toFile())
                val event = watchEvent.kind()
                timeout?.cancel()
                timeout = launch {
                    logger.debug("event: $path, ${event.name()}")
                    delay(1000)

                    when (watchEvent.kind().name()) {
                        "ENTRY_CREATE" -> {
                            logger.debug("$path was created")
                            logger.info("content: '${file.readText()}'")
                        }
                        "ENTRY_MODIFY" -> {
                            logger.debug("$path was modified")
                            logger.info("content: '${file.readText()}'")
                        }
                        "ENTRY_DELETE" -> {
                            logger.debug("$path was deleted")
                        }
                        "OVERFLOW" -> logger.debug("${watchEvent.context()} overflow")
                    }
                }
            }
        }
        logger.trace("started notebook watcher")
        return watchJob
    }
}