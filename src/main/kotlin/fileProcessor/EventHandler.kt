package com.seansoper.zebec.fileProcessor

import com.seansoper.zebec.Blog
import com.seansoper.zebec.Utilities.humanReadableByteCount
import com.seansoper.zebec.WatchFile
import com.seansoper.zebec.configuration.Settings
import com.seansoper.zebec.filenameNoExtension
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

interface Processable {
    fun process(content: String): String?
}

class EventHandler(val changed: WatchFile.ChangedFile, val settings: Settings) {

    private data class ProcessedFile(val content: String, val fullname: String)

    private val destinationDirectory: File?
        get() {
            val relativeDestination = changed.path.toString().split(settings.source.toString()).elementAtOrNull(1) ?: return null
            val directory = Paths.get(settings.destination.toString(), relativeDestination)
            return File(directory.toString().replace("/./", "/")).parentFile
        }

    enum class DocType {
        KTML,
        JavaScript,
        Stylesheet,
        Markdown,
        BlogEntry;

        fun compiledFilename(filename: String): String? {
            return when (this) {
                KTML, Markdown, BlogEntry -> "$filename.html"
                JavaScript -> "$filename.min.js"
                Stylesheet -> "$filename.min.css"
            }
        }
    }

    fun process(done: (Path?) -> Unit) {
        val path = processFile { filename, extension, content ->
            when (extension) {
                // TODO: Replace with Type
                "ktml" -> KTML(settings.verbose).process(content)?.let {
                    ProcessedFile(it, "$filename.html")
                }
                "js" -> Script(Script.Type.javascript, settings.verbose).process(content)?.let {
                    ProcessedFile(it, "$filename.min.$extension")
                }
                "css" -> Script(Script.Type.stylesheet, settings.verbose).process(content)?.let {
                    ProcessedFile(it, "$filename.min.$extension")
                }
                "md" -> Markdown(settings).process(content)?.let {
                    ProcessedFile(it, "$filename.html")
                }
                else -> {
                    if (settings.verbose) {
                        println("ERROR: Unsupported content type $extension")
                    }

                    null
                }
            }
        }

        done(path)
    }

    private fun processFile(transform: (filename: String, extension: String, content: String) -> ProcessedFile?): Path? {
        val filename = changed.path.filenameNoExtension() ?: return null
        val destination = destinationDirectory ?: return null
        val content = File(changed.path.toString()).readText()

        println(Blog(settings).isBlogEntry(changed))
        println("@@@")

        // TODO: Get parent dir path before calling transform
        // TODO: Get type here
        val result = transform(filename, changed.extension, content) ?: return null

        if (!destination.exists()) {
            destination.mkdirs()
        }

        val path = Paths.get(destination.toString(), result.fullname)

        if (settings.verbose) {
            val origSize = humanReadableByteCount(content.length)
            val newSize = humanReadableByteCount(result.content.length)
            println("Compiled ${changed.path.fileName}, $origSize → $newSize")
        }

        Files.write(path, result.content.toByteArray())
        return path
    }
}