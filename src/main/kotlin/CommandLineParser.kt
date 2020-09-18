package com.seansoper.zebec

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

// TODO: Remove unnecessary true/false flags for arguments
class CommandLineParser(private val args: Array<String>,
                        private val basePath: String = System.getProperty("user.dir")) {

    val shouldShowHelp: Boolean = args.any { it == "-help" }
    val verbose: Boolean = args.any { it == "-verbose" }
    val recompile: Boolean = args.any { it == "-recompile" }

    data class Parsed(val pathToConfigFile: Path,
                      val verbose: Boolean,
                      val recompile: Boolean)

    fun showHelp() {
        val str = """
            Arguments

                -help              Show documentation
                -verbose           Show debugging output
                -recompile         Recompile the files instead of running the service
                -config=path       Path to configuration file, default is ./zebec.config
        """.trimIndent()
        println(str)
    }

    fun parse(): Parsed {
        val pathToConfigFile = getPath("config") ?: Paths.get(basePath,"zebec.config")

        if (!File(pathToConfigFile.toString()).exists()) {
            throw ConfigFileNotFound()
        }

        return Parsed(pathToConfigFile, verbose, recompile)
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

    private fun getPath(type: String): Path? {
        val regex = Regex("^-${type}=(.*)")

        return parseArguments(regex) {
            if (it.startsWith("/")) {
                Paths.get(it)
            } else {
                Paths.get(basePath, it)
            }
        }.firstOrNull()
    }

}

class ConfigFileNotFound: Exception("Zebec config file not found")