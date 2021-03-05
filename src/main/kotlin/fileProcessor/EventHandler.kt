package com.seansoper.zebec.fileProcessor

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
            val blog = settings.blog ?: return null

            if (blog.isBlogEntry(changed)) {
                return DocType.BlogEntry
            }

            return DocType.getFor(changed.extension)
        }

    val destination: Path?
        get() {
            val docType = docType ?: return null
            val filename = changed.path.filenameNoExtension() ?: return null
            val destinationDirectory = destinationDirectory ?: return null

            return Paths.get(destinationDirectory.toString(), docType.compiledFilename(filename))
        }

    private val relativeDestination: String?
        get() {
            val prefix = changed.path.toString().commonPrefixWith(destination.toString())
            val path = destination.toString().removePrefix(prefix).replace("/./", "/")

            return if (path.startsWith("/")) {
                path
            } else {
                "/${path}"
            }
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
                    "ktml"  -> KTML
                    "js"    -> JavaScript
                    "css"   -> Stylesheet
                    "md"    -> Markdown
                    else    -> null
                }
            }
        }
    }

    fun process(done: (Path?) -> Unit) {
        val path = processFile { content ->
            when (docType) {
                DocType.BlogEntry -> processBlog(content)
                DocType.KTML -> KTML(settings.verbose).process(content)
                DocType.JavaScript -> Script(Script.Type.javascript, settings.verbose).process(content)
                DocType.Stylesheet -> Script(Script.Type.stylesheet, settings.verbose).process(content)
                DocType.Markdown -> Markdown().process(content)
                else -> null
            }
        }

        done(path)
    }

    private fun processFile(transform: (content: String) -> String?): Path? {
        destinationDirectory?.apply {
            if (!this.exists()) {
                this.mkdirs()
            }
        }

        val content = File(changed.path.toString()).readText()
        val compiled = transform(content) ?: return null

        if (settings.verbose) {
            val origSize = humanReadableByteCount(content.length)
            val newSize = humanReadableByteCount(compiled.length)
            println("Compiled ${changed.path.fileName}, $origSize → $newSize")
        }

        return destination?.let {
            Files.write(it, compiled.toByteArray())
            destination
        }
    }

    private fun processBlog(content: String): String? {
        return settings.blog?.let {
            BlogEntry(it, changed.path, relativeDestination, settings.verbose).process(content)
        } ?: run {
            if (settings.verbose) {
                println("Could not find blog configurtation")
            }
            null
        }
    }
}