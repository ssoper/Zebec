package com.seansoper.zebec

import kotlinx.coroutines.runBlocking
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

object Core {

    const val DefaultPort = 8080

    @JvmStatic fun main(args: Array<String>) = runBlocking {
        if (shouldShowHelp(args)) {
            showHelp()
            exitProcess(0)
        }

        val paths = getWatchPaths(args)

        if (paths.isEmpty()) {
            println("ERROR: No watch paths specified\n")
            showHelp()
            exitProcess(1)
        }

        val port = getPort(args)
        val extensions = getExtension(args)

        val watch = try {
            WatchFile(paths, extensions)
        } catch (exception: NoSuchFileException) {
            println("ERROR: watch argument invalid for ${exception.file}")
            exitProcess(1)
        }

        val channel = watch.createChannel()
        val verbose = getVerbose(args)

        if (verbose) {
            println("Serving at localhost:$port")
            watch.paths.forEach { println("Watching $it") }
            println("Filtering on files with extensions ${extensions.joinToString()}")
        }

        while (true) {
            if (verbose) {
                println("Change detected at ${channel.receive()}")
            }
        }
    }

    private fun<T: Any> parseArguments(regex: Regex, args: Array<String>, transform: (String) -> T): List<T> {
        val match = fun (str: String): T? {
            return regex.find(str)?.let {
                if (it.groups.count() < 2) {
                    return null
                }

                it.groups[1]?.let {
                    return transform(it.value)
                }
            }
        }

        return args.mapNotNull(match)
    }

    private fun getExtension(args: Array<String>): List<String> {
        val regex = Regex("^-extension=(\\w{3,4})")
        val extensions = parseArguments(regex, args) { it }

        return if (extensions.isEmpty()) {
            listOf("css", "js", "html")
        } else {
            extensions
        }
    }

    private fun getVerbose(args: Array<String>): Boolean {
        val regex = Regex("^-verbose=(\\w+)")
        return parseArguments(regex, args) { it == "true" }.isNotEmpty()
    }

    private fun getPort(args: Array<String>): Int {
        val regex = Regex("^-port=(\\d{2,5})")
        val results = parseArguments(regex, args) { it.toInt() }

        return if (results.isEmpty()) {
            DefaultPort
        } else {
            results[0]
        }
    }

    private fun getWatchPaths(args: Array<String>): List<Path> {
        val basePath = System.getProperty("user.dir")
        val regex = Regex("^-watch=(.*)")

        return parseArguments(regex, args) { Paths.get(basePath, it.removeSurrounding("\"")) }
    }

    private fun shouldShowHelp(args: Array<String>): Boolean {
        return args.any { it == "-help" }
    }

    private fun showHelp() {
        val str = """
            Arguments

                -help                   Show documentation
                -watch="dir/to/watch"   Relative directory path to watch for file changes, each specified path should have its own `-watch` (Required)
                -extension=filetype     File type (extension) to filter on, defaults are css, js and html
                -port=8080              Port for server, default is 8080
                -verbose=true           Show debugging output
        """.trimIndent()
        println(str)
    }
}