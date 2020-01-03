package com.seansoper.zebec

import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.abs
import kotlin.system.exitProcess


object Core {

    private const val DefaultPort = 8080
    private val BasePath = System.getProperty("user.dir")
    private var Verbose = false

    @JvmStatic fun main(args: Array<String>) = runBlocking {
        if (shouldShowHelp(args)) {
            showHelp()
            exitProcess(0)
        }

        val source = getPath("source", args) ?: run {
            println("ERROR: No source path specified\n")
            showHelp()
            exitProcess(1)
        }

        val dest = getPath("dest", args) ?: Paths.get(".")

        val port = getPort(args)
        val extensions = getExtension(args)

        val watch = try {
            WatchFile(listOf(source), extensions)
        } catch (exception: NoSuchFileException) {
            println("ERROR: watch argument invalid for ${exception.file}")
            exitProcess(1)
        }

        val channel = watch.createChannel()
        Verbose = getVerbose(args)

        if (Verbose) {
            println("Serving at localhost:$port")
            watch.paths.forEach { println("Watching $it") }
            println("Filtering on files with extensions ${extensions.joinToString()}")
        }

        while (true) {
            val changed = channel.receive()

            if (Verbose) {
                println("Change detected at ${changed.path}")
            }

            process(changed, source, dest) { filename, extension, content ->
                val fullname = "$filename.$extension.compiled"
                ProcessedFile(content, fullname)
            }
        }
    }

    private data class ProcessedFile(val content: String, val fullname: String)

    private fun process(changed: WatchFile.ChangedFile, source: Path, dest: Path, transform: (filename: String, extension: String, content: String) -> ProcessedFile) {
        val destDir = changed.path.toString().split(source.toString()).elementAtOrNull(1) ?: return
        val finalDest = Paths.get(BasePath, dest.toString(), destDir)
        val filename = finalDest.fileName.toString().split(".").firstOrNull() ?: return
        val content = File(changed.path.toString()).readText()
        val result = transform(filename, changed.extension, content)
        val finalDestParentDir = File(finalDest.toString().replace("/./", "/")).parentFile

        if (!finalDestParentDir.exists()) {
            finalDestParentDir.mkdirs()
        }

        val path = Paths.get(finalDestParentDir.toString(), result.fullname)

        if (Verbose) {
            val origSize = humanReadableByteCount(content.length.toLong())
            val newSize = humanReadableByteCount(result.content.length.toLong())
            println("Compiled ${finalDest.fileName} ($origSize) to $path ($newSize)")
        }

        Files.write(path, result.content.toByteArray())
    }

    // Credit: https://stackoverflow.com/a/59234917
    private fun humanReadableByteCount(bytes: Long): String = when {
        bytes == Long.MIN_VALUE || bytes < 0 -> "N/A"
        bytes < 1024L -> "$bytes bytes"
        bytes <= 0xfffccccccccccccL shr 40 -> "%.1f KB".format(bytes.toDouble() / (0x1 shl 10))
        bytes <= 0xfffccccccccccccL shr 30 -> "%.1f MB".format(bytes.toDouble() / (0x1 shl 20))
        bytes <= 0xfffccccccccccccL shr 20 -> "%.1f GB".format(bytes.toDouble() / (0x1 shl 30))
        bytes <= 0xfffccccccccccccL shr 10 -> "%.1f TB".format(bytes.toDouble() / (0x1 shl 40))
        bytes <= 0xfffccccccccccccL -> "%.1f PiB".format((bytes shr 10).toDouble() / (0x1 shl 40))
        else -> "%.1f EB".format((bytes shr 20).toDouble() / (0x1 shl 40))
    }

    private fun<T: Any> parseArguments(regex: Regex, args: Array<String>, transform: (String) -> T): List<T> {
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

    private fun getExtension(args: Array<String>): List<String> {
        val regex = Regex("^-extension=(\\w{3,4})")
        val extensions = parseArguments(regex, args) { it }

        return if (extensions.isEmpty()) {
            listOf("css", "js", "ktml")
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

        return results.firstOrNull() ?: DefaultPort
    }

    private fun getPath(type: String, args: Array<String>): Path? {
        val regex = Regex("^-${type}=(.*)")

        return parseArguments(regex, args) { Paths.get(BasePath, it) }.firstOrNull()
    }

    private fun shouldShowHelp(args: Array<String>): Boolean {
        return args.any { it == "-help" }
    }

    private fun showHelp() {
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
}