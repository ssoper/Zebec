package com.seansoper.zebec

import com.seansoper.zebec.configuration.BlogConfiguration
import com.seansoper.zebec.configuration.Settings
import com.seansoper.zebec.fileProcessor.BlogEntry
import com.seansoper.zebec.fileProcessor.EventHandler
import com.seansoper.zebec.fileProcessor.KTML
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.streams.toList

class Blog(configuration: BlogConfiguration, val verbose: Boolean = false) {

    val directory: Path = configuration.directory
    val extension: String = configuration.extension

    data class Template(val path: Path, val raw: String, val compiled: String) {
        fun render(content: String): String {
            return compiled.replace("<zebeccontent />", content)
        }
    }

    data class Entry(val filePath: Path, val relativePath: String, val createdDate: LocalDateTime, val metadata: BlogEntry.Metadata) {
        fun html(): String {
            val image = metadata.image?.let {
                "<img class='card-img-top' src='${it.previewUrlNormal}' srcset='${it.previewUrlNormal} 1x, ${it.previewUrlRetina} 2x' />"
            } ?: metadata.imageURL?.let {
                "<img class='card-img-top' src='${it.relativeProtocol}' />"
            } ?: ""

            return """
                <div class='card mb-4'>
                    $image
                    <div class='card-body p-3'>
                      <h5 class='card-title m-0'>${metadata.title}</h5>
                      <p class='card-text'>${metadata.firstParagraph}</p>
                      <a href='${relativePath}' class='stretched-link'></a>
                    </div>
                  </div>
            """.trimIndent()
        }
    }

    lateinit var template: Template

    init {
        val path = configuration.template
        val raw = File(path.toString()).readText()

        try {
            KTML(verbose).process(raw)?.let {
                template = Template(path, raw, it)
            } ?: throw ErrorCompilingBlogTemplate(path)
        } catch (exception: FileNotFoundException) {
            throw MissingBlogTemplate(path)
        }
    }

    // TODO: Remove dependence on having HTML code mixed with Kotlin
    // TODO: Break up into smaller components
    fun recompile(settings: Settings) {
        val paths = Files.walk(directory, 1).filter {
            val file = it.toFile()
            !file.isDirectory && file.extension == extension
        }.toList()

        val suffix = "found in $directory with extension $extension"

        if (paths.count() < 1) {
            if (verbose) {
                println("Zero files $suffix")
            }

            return
        }

        if (verbose) {
            println("${paths.count()} files $suffix")
        }

        var entries = emptyArray<Entry>()
        val now = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime()

        paths.forEach { path ->
            val file = WatchFile.ChangedFile(path, extension)
            val metadata = BlogEntry(this, path, verbose).getMetadata() ?: return@forEach
            val relativePath = relativeDestinationDir(settings.source, path) ?: return@forEach

            EventHandler(file, settings).process {
                it?.let {
                    if (verbose) {
                        println("Compiled $path to $it")
                    }

                    val createdDate = path.createdDate ?: now
                    entries += Entry(it, relativePath, createdDate, metadata)
                } ?: run {
                    if (verbose) {
                        println("Failed to compile $path")
                    }
                }
            }
        }

        val path = entries.firstOrNull()?.filePath?.let { Paths.get(it.parent.toString(), "index.html") } ?: return

        var count = 0
        var html = "<div class='card-deck mt-4'>"
        entries.sortedBy { it.createdDate }.forEach {
            html += "\n${it.html()}"
            if (++count % 2 == 0) {
                html += "\n<div class='w-100 d-none d-sm-block d-md-block'></div>"
            }
        }

        html += "\n</div>"
        val result = template.render(html)

        Files.write(path, result.toByteArray())

        if (verbose) {
            println("Wrote $path with ${entries.count()} entries")
        }
    }

    fun isBlogEntry(changedFile: WatchFile.ChangedFile): Boolean {
        val directory = File(changedFile.path.toString()).parentFile.toPath()

        return (directory == directory &&
                extension == changedFile.extension)
    }

    // TODO: This shares some functionality with EventHandler.processFile, consider consolidating
    private fun relativeDestinationDir(source: Path, changed: Path): String? {
        val dir = changed.toString().split(source.toString()).elementAtOrNull(1) ?: return null
        val path = dir.
            replace("/./", "/").
            replace(Regex(".\\w+$"), ".html")

        return if (path.startsWith("/")) {
            path
        } else {
            "/${path}"
        }
    }

}

class MissingBlogTemplate(path: Path): Exception("Could not find template file $path")
class ErrorCompilingBlogTemplate(path: Path): Exception("Could not compile template file $path")