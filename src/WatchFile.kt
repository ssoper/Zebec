import sun.misc.Signal
import sun.misc.SignalHandler
import java.lang.Thread.sleep
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.util.concurrent.atomic.AtomicBoolean

fun main(args: Array<String>) {
    val watchService = FileSystems.getDefault().newWatchService()
    val path = Paths.get("/Users/ssoper/workspace/StaticSite")
    path.register(watchService, ENTRY_MODIFY)

    println(path)

    while (true) {
        println("watching")
        val key = watchService.take()
        key.pollEvents().forEach {
            println(it.context())
        }
        key.reset()

    }

//    val running: AtomicBoolean = AtomicBoolean(true)
//
//    while (running.get()) {
//        sleep(1000)
//        print("hey there")
//    }

//    val handler: SignalHandler = SignalHandler {
//        println("oh yea")
//        running.set(false)
//    }
//
//    Signal.handle(Signal("INT"), handler)
}