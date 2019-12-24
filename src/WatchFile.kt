import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchService

fun registerPaths(paths: Array<String>): WatchService {
    val watchService = FileSystems.getDefault().newWatchService()

    if (paths.count() > 1) {
        val otherPaths = paths.sliceArray(IntRange(1, paths.count()-1))
        val watchPath = Paths.get(paths.first(), *otherPaths)
        watchPath.register(watchService, ENTRY_MODIFY)
    } else {
        val watchPath = Paths.get(paths.first())
        watchPath.register(watchService, ENTRY_MODIFY)
    }

    return watchService
}

fun main(args: Array<String>) {

    val paths = arrayOf(
        "/Users/ssoper/workspace/StaticSite/",
        "/Users/ssoper/workspace/StaticSite/css",
        "/Users/ssoper/workspace/StaticSite/out"
    )

    val watchService = registerPaths(paths)

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
}