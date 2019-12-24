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

    val mine = arrayOf("sean", "is", "awesome")
    val otherPaths = mine.sliceArray(IntRange(1, mine.count()-1))
    mine.forEach { println(it) }
    println("oh hai")
    otherPaths.forEach { println(it) }

/*
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

    }*/
}