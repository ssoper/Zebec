import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY

fun main(args: Array<String>) {
    val watchService = FileSystems.getDefault().newWatchService()
    val path = Paths.get("/Users/ssoper/workspace/StaticSite/css")
    path.register(watchService, ENTRY_MODIFY)

    println(path)

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