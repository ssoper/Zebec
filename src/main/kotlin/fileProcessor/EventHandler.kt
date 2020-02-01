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

    private val destinationDirectory: File?
        get() {
            val relativeDestination = changed.path.toString().split(settings.source.toString()).elementAtOrNull(1) ?: return null
            val directory = Paths.get(settings.destination.toString(), relativeDestination)
            return File(directory.toString().replace("/./", "/")).parentFile
        }

    private val docType: DocType?
        get() {
            if (Blog(settings).isBlogEntry(changed)) {
                return DocType.BlogEntry
            }

            return DocType.getFor(changed.extension)
        }

    enum class DocType {
        KTML,
        JavaScript,
        Stylesheet,
        Markdown,
        BlogEntry;

        fun compiledFilename(filename: String): String {
            return when (this) {
                KTML, Markdown, BlogEntry -> "$filename.html"
                JavaScript -> "$filename.min.js"
                Stylesheet -> "$filename.min.css"
            }
        }

        companion object {
            fun getFor(extension: String): DocType? {
                return when (extension) {
                    "ktml" -> KTML
                    "js" -> JavaScript
                    "css" -> Stylesheet
                    "md" -> Markdown
                    else -> null
                }
            }
        }
    }

    fun process(done: (Path?) -> Unit) {
        val path = processFile { docType, content ->
            when (docType) {
                DocType.BlogEntry -> BlogEntry(settings).process(content)
                DocType.KTML -> KTML(settings.verbose).process(content)
                DocType.JavaScript -> Script(Script.Type.javascript, settings.verbose).process(content)
                DocType.Stylesheet -> Script(Script.Type.stylesheet, settings.verbose).process(content)
                DocType.Markdown -> Markdown(settings).process(content)
            }
        }

        done(path)
    }

    private fun processFile(transform: (docType: DocType, content: String) -> String?): Path? {
        val docType = docType ?: return null
        val filename = changed.path.filenameNoExtension() ?: return null
        val destination = destinationDirectory ?: return null
        val content = File(changed.path.toString()).readText()
        val compiled = transform(docType, content) ?: return null

        if (!destination.exists()) {
            destination.mkdirs()
        }

        val path = Paths.get(destination.toString(), docType.compiledFilename(filename))

        if (settings.verbose) {
            val origSize = humanReadableByteCount(content.length)
            val newSize = humanReadableByteCount(compiled.length)
            println("Compiled ${changed.path.fileName}, $origSize → $newSize")
        }

        Files.write(path, compiled.toByteArray())
        return path
    }
}