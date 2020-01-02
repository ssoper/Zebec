package com.seansoper.zebec

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY
import java.nio.file.WatchService

class WatchFile(val paths: List<Path>, val extensions: List<String>) {
    val watchService: WatchService = FileSystems.getDefault().newWatchService()

    init {
        paths.forEach {
            it.register(watchService, ENTRY_MODIFY)
        }
    }

    suspend fun createChannel(): Channel<String> {
        val channel = Channel<String>()
        val scope: CoroutineScope = GlobalScope

        scope.launch(Dispatchers.IO) {
            while (true) {
                val key = watchService.take()
                val dirPath = key.watchable() as? Path ?: break
                key.pollEvents().forEach {
                    val path = dirPath.resolve(it.context() as Path)
                    if (matches(path)) {
                        channel.send(path.toString())
                    }
                }
                key.reset()
            }
        }

        return channel
    }

    private fun matches(path: Path): Boolean {
        return extensions.any { path.toString().toLowerCase().endsWith(".${it.toLowerCase()}") }
    }

}