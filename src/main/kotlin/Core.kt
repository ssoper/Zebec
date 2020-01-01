package com.seansoper.zebec

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

object Core {

    const val DefaultPort = 8080

    @JvmStatic fun main(args: Array<String>) {
        if (shouldShowHelp(args)) {
            showHelp()
            exitProcess(0)
        }

        val watchPaths = getWatchPaths(args)

        if (watchPaths.isEmpty()) {
            println("ERROR: No watch paths specified\n")
            showHelp()
            exitProcess(1)
        }

        val port = getPort(args)

        println("***")
        println(watchPaths)
        println(port)
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
                -port=8080              Port for server, default is 8080
        """.trimIndent()
        println(str)
    }
}