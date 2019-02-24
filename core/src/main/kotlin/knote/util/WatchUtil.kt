package knote.util

import knote.KNote
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ActorScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchService

enum class WatchEvents {
    ENTRY_CREATE,
    ENTRY_MODIFY,
    OVERFLOW,
    ENTRY_DELETE
}

fun watchActor(path: Path, actorScope: suspend ActorScope<WatchEvent<Path>>.() -> Unit): Job {
    val actor = GlobalScope.actor(block = actorScope)
    val job = GlobalScope.launch {
        val watcher = path.watch()
        while (true) {
            //The watcher blocks until an event is available
            val key = watcher.take()

            if(!isActive) key.cancel()

            //Now go through each event on the folder
            key.pollEvents().forEach { it ->
                actor.send(it as WatchEvent<Path>)
            }
            //Call reset() on the key to watch for future events
            key.reset()
        }
    }
    KNote.cancelOnShutDown(job)
    return job
}

internal fun Path.watch(): WatchService {
    //Create a watch service
    val watchService = this.fileSystem.newWatchService()

    //Register the service, specifying which events to watch
    register(
        watchService,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.OVERFLOW,
        StandardWatchEventKinds.ENTRY_DELETE
    )

    //Return the watch service
    return watchService
}