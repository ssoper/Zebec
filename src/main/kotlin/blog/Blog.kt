package com.seansoper.zebec.blog

import com.seansoper.zebec.WatchFile
import com.seansoper.zebec.configuration.BlogConfiguration
import com.seansoper.zebec.configuration.Settings
import com.seansoper.zebec.fileProcessor.EventHandler
import com.seansoper.zebec.fileProcessor.KTML
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

class Blog(configuration: BlogConfiguration, val host: String? = null, val verbose: Boolean = false) {

    val directory: Path = configuration.directory
    val extension: String = configuration.extension

    data class Template(val path: Path, val raw: String, val compiled: String) {
        fun render(content: String): String {
            return compiled.replace("<zebeccontent />", content)
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
        val paths = getPaths()

        if (verbose) {
            println("Found ${paths.count()} files in $directory with extension $extension")
        }

        if (paths.count() < 1) {
            return
        }

        var entries = emptyArray<BlogEntryMetadata>()
        var compiledBlogPath: Path? = null

        paths.forEach { path ->
            val file = WatchFile.ChangedFile(path, extension)
            val metadata = try {
                BlogEntryMetadata(path)
            } catch (exception: Exception) {
                if (verbose) {
                    println("Failed to compile $path, $exception")
                }

                return@forEach
            }

            EventHandler(file, settings).process {
                it?.let {
                    if (verbose) {
                        println("Compiled $path to $it")
                    }

                    if (compiledBlogPath == null) {
                        compiledBlogPath = it
                    }

                    entries += metadata
                } ?: run {
                    if (verbose) {
                        println("Failed to compile $path")
                    }
                }
            }
        }

        var count = 0
        var html = "<div class='card-deck mt-4'>"

        entries.sortedBy { it.createdDate }.reversed().forEach { metadata ->
            val relativePath = compiledBlogPath?.let { relativePath(settings.source, it) } ?: return@forEach
            html += "\n${metadata.previewHtml(relativePath)}"
            if (++count % 2 == 0) {
                html += "\n<div class='w-100 d-none d-sm-block d-md-block'></div>"
            }
        }

        html += "\n</div>"

        val result = template.render(html)
        val indexPath = compiledBlogPath?.let { Paths.get(it.parent.toString(), "index.html") } ?: return
        Files.write(indexPath, result.toByteArray())

        if (verbose) {
            println("Wrote $indexPath with ${entries.count()} entries")
        }
    }

    fun isBlogEntry(changedFile: WatchFile.ChangedFile): Boolean {
        val directory = File(changedFile.path.toString()).parentFile.toPath()

        return (directory == directory &&
                extension == changedFile.extension)
    }

    // TODO: This shares some functionality with EventHandler.processFile, consider consolidating
    private fun relativePath(source: Path, compiledBlogPath: Path): String? {
        val prefix = source.toString().commonPrefixWith(compiledBlogPath.toString())
        val path = compiledBlogPath.toString().removePrefix(prefix).replace("/./", "/")

        return if (path.startsWith("/")) {
            path
        } else {
            "/${path}"
        }
    }

    private fun getPaths(): List<Path> {
        return Files.walk(directory, 1).filter {
            val file = it.toFile()
            !file.isDirectory && file.extension == extension
        }.toList()
    }

}

class MissingBlogTemplate(path: Path): Exception("Could not find template file $path")
class ErrorCompilingBlogTemplate(path: Path): Exception("Could not compile template file $path")