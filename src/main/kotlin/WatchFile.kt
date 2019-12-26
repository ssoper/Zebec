import kotlinx.coroutines.channels.Channel
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchService

fun registerPaths(paths: Array<String>): List<Channel<String>> {
    return paths.map {
        val channel = Channel<String>()
        val watchService = FileSystems.getDefault().newWatchService()
        val watchPath = Paths.get(it)
        watchPath.register(watchService, ENTRY_MODIFY)

        while (true) {
            println("watching")
            val key = watchService.take()
            val dirPath = key.watchable() as? Path ?: break
            key.pollEvents().forEach {
                val eventPath = dirPath.resolve(it.context() as Path)
                println("Path $eventPath, context $it.context()")
            }
            key.reset()

        }

        return listOf(channel)
    }

}

fun main(args: Array<String>) {

    val paths = arrayOf(
        "/Users/ssoper/workspace/StaticSite/css"
    )

    val watchService = registerPaths(paths)

}