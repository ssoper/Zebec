import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchService

fun registerPaths(paths: Array<String>): WatchService {
    val watchService = FileSystems.getDefault().newWatchService()

    paths.forEach {
        val watchPath = Paths.get(it)
        watchPath.register(watchService, ENTRY_MODIFY)
    }

    return watchService
}

suspend fun createChannel(service: WatchService): Channel<String> {
    val channel = Channel<String>()
    val scope: CoroutineScope = GlobalScope

    scope.launch(Dispatchers.IO) {
        while (true) {
            val key = service.take()
            val dirPath = key.watchable() as? Path ?: break
            key.pollEvents().forEach {
                val eventPath = dirPath.resolve(it.context() as Path)
                channel.send(eventPath.toString())
            }
            key.reset()
        }
    }

    return channel
}

fun main() = runBlocking {

    val paths = arrayOf(
        "/Users/ssoper/workspace/StaticSite/css",
        "/Users/ssoper/workspace/StaticSite/js"
    )

    val service = registerPaths(paths)
    val channel = createChannel(service)

    while (true) {
        println("Path ${channel.receive()}")
    }
}