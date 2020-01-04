package com.seansoper.zebec

import java.nio.file.Path
import java.nio.file.Paths

class CommandLineParser(private val args: Array<String>,
                        private val basePath: String = System.getProperty("user.dir"),
                        private val defaultPort: Int = 8080) {

    val shouldShowHelp: Boolean = args.any { it == "-help" }
    var errorMessage: String? = null

    data class Parsed(val source: Path, val dest: Path, val port: Int, val extensions: List<String>, val verbose: Boolean)

    fun showHelp() {
        val str = """
            Arguments

                -help                   Show documentation
                -source=directory       Relative directory path for source of project files (Required)
                -dest=directory         Relative directory path for destination of compiled files, default is current working directory
                -extension=filetype     File extension to filter on. Each specified file type should have its own `-extension` argument.
                                        Defaults are css, js and ktml.
                -port=8080              Port for server, default is 8080
                -verbose=true           Show debugging output
        """.trimIndent()
        println(str)
    }

    fun parse(): Parsed? {
        val source = getPath("source") ?: run {
            errorMessage = "ERROR: No source path specified"
            return null
        }

        val dest = getPath("dest") ?: Paths.get(basePath,".")
        val port = getPort()
        val extensions = getExtension()
        val verbose = getVerbose()

        return Parsed(source, dest, port, extensions, verbose)
    }

    private fun<T: Any> parseArguments(regex: Regex, transform: (String) -> T): List<T> {
        val match = fun (str: String): T? {
            return regex.find(str)?.let {
                if (it.groups.count() < 2) {
                    return null
                }

                it.groups[1]?.let {
                    return transform(it.value.removeSurrounding("\"").removeSurrounding("'"))
                }
            }
        }

        return args.mapNotNull(match)
    }

    private fun getExtension(): List<String> {
        val regex = Regex("^-extension=(\\w{3,4})")
        val extensions = parseArguments(regex) { it }

        return if (extensions.isEmpty()) {
            listOf("css", "js", "ktml")
        } else {
            extensions
        }
    }

    private fun getVerbose(): Boolean {
        val regex = Regex("^-verbose=(\\w+)")
        return parseArguments(regex) { it == "true" }.isNotEmpty()
    }

    private fun getPort(): Int {
        val regex = Regex("^-port=(\\d{2,5})")
        val results = parseArguments(regex) { it.toInt() }

        return results.firstOrNull() ?: defaultPort
    }

    private fun getPath(type: String): Path? {
        val regex = Regex("^-${type}=(.*)")

        return parseArguments(regex) { Paths.get(basePath, it) }.firstOrNull()
    }

}