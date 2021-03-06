package com.seansoper.zebec

import com.seansoper.zebec.blog.Serializer
import com.seansoper.zebec.configuration.Settings
import com.seansoper.zebec.fileProcessor.EventHandler
import kotlinx.coroutines.runBlocking
import java.nio.file.NoSuchFileException
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

        if (parsed.recompile) {
            val blog = settings.blog ?: run {
                println("ERROR: Recompile flag passed but no blog configuration found")
                exitProcess(1)
            }

            blog.recompile(settings)

            val serializer = Serializer(settings)
            if (serializer.generateFeed()) {
                println("Successfully wrote RSS file")
            } else {
                println("Failed to write RSS file")
            }

            exitProcess(0)
        }

        val server = ContentServer(settings.destination, settings.port, settings.verbose)
        server.start()

        runBlocking {
            watchFiles(settings, server)
        }
    }

    suspend fun watchFiles(settings: Settings, server: ContentServer) {
        val watch = try {
            WatchFile(listOf(settings.source), settings.extensions.toList())
        } catch (exception: NoSuchFileException) {
            println("ERROR: watch argument invalid for ${exception.file}")
            exitProcess(1)
        }

        val channel = watch.createChannel()

        watch.paths.forEach { println("Watching $it") }
        println("Filtering on files with extensions ${settings.extensions.joinToString()}")

        while (true) {
            val changed = channel.receive()

            if (settings.verbose) {
                println("Change detected at ${changed.path}")
            }

            EventHandler(changed, settings).process {
                if (settings.verbose) {
                    it?.let {
                        println("Copied to $it")
                    } ?: println("Failed to compile")
                }

                server.refresh()
            }
        }
    }
}