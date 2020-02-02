package com.seansoper.zebec

import com.seansoper.zebec.configuration.BlogConfiguration
import com.seansoper.zebec.configuration.Settings
import com.seansoper.zebec.fileProcessor.BlogEntry
import com.seansoper.zebec.fileProcessor.KTML
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path

class Blog(configuration: BlogConfiguration, val verbose: Boolean = false) {

    val directory: Path = configuration.directory
    val extension: String = configuration.extension

    data class Template(val path: Path, val raw: String, val compiled: String)

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

    fun recompile() {
        val files = Files.walk(directory, 1).filter {
            val file = it.toFile()
            !file.isDirectory && file.extension == extension
        }

        val suffix = "found in ${directory} with ${extension}"

        if (files.count() < 1) {
            if (verbose) {
                println("Zero files $suffix")
            }

            return
        }

        if (verbose) {
            println("${files.count()} files found in $suffix")
        }


    }

    fun isBlogEntry(changedFile: WatchFile.ChangedFile): Boolean {
        val directory = File(changedFile.path.toString()).parentFile.toPath()

        return (directory == directory &&
                extension == changedFile.extension)
    }

}

class MissingBlogTemplate(path: Path): Exception("Could not find template file $path")
class ErrorCompilingBlogTemplate(path: Path): Exception("Could not compile template file $path")