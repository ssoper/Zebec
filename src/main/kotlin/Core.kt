package com.seansoper.zebec

import com.seansoper.zebec.configuration.Settings
import com.seansoper.zebec.fileProcessor.EventHandler
import kotlinx.coroutines.runBlocking
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.system.exitProcess

object Core {

    @JvmStatic fun main(args: Array<String>) {
        val cli = CommandLineParser(args)

        if (cli.shouldShowHelp) {
            cli.showHelp()
            exitProcess(0)
        }

        val parsed = try {
            cli.parse()
        } catch (exception: ConfigFileNotFound) {
            cli.showHelp()
            exitProcess(1)
        }

        val settings = Settings(parsed, System.getProperty("user.dir"))

        val server = ContentServer(settings.destination, settings.port, settings.verbose)
        server.start()

        runBlocking {
            watchFiles(settings.source, settings.destination, settings.extensions.toList(), server, settings.verbose)
        }
    }

    suspend fun watchFiles(source: Path, dest: Path, extensions: List<String>, server: ContentServer, verbose: Boolean) {
        val watch = try {
            WatchFile(listOf(source), extensions)
        } catch (exception: NoSuchFileException) {
            println("ERROR: watch argument invalid for ${exception.file}")
            exitProcess(1)
        }

        val channel = watch.createChannel()

        if (verbose) {
            watch.paths.forEach { println("Watching $it") }
            println("Filtering on files with extensions ${extensions.joinToString()}")
        }

        while (true) {
            val changed = channel.receive()

            if (verbose) {
                println("Change detected at ${changed.path}")
            }

            EventHandler(changed, source, dest, verbose).process {
                if (verbose) {
                    it?.let {
                        println("Copied to $it")
                    } ?: println("Failed to compile")
                }

                server.refresh()
            }
        }
    }
}