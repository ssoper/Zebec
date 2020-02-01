package com.seansoper.zebec

import com.seansoper.zebec.configuration.Settings
import java.io.File
import java.nio.file.Files

class Blog(val settings: Settings) {

    fun recompile() {
        val configuration = settings.blog?.let { it } ?: throw MissingBlogConfiguration()
        val files = Files.walk(configuration.directory, 1).filter {
            val file = it.toFile()
            !file.isDirectory && file.extension == configuration.extension
        }

        val suffix = "found in ${configuration.directory} with ${configuration.extension}"

        if (files.count() < 1) {
            if (settings.verbose) {
                println("Zero files $suffix")
            }

            return
        }

        if (settings.verbose) {
            println("${files.count()} files found in $suffix")
        }


    }

    fun isBlogEntry(changedFile: WatchFile.ChangedFile): Boolean {
        val blog = settings.blog ?: return false
        val directory = File(changedFile.path.toString()).parentFile.toPath()

        return (blog.directory == directory &&
                blog.extension == changedFile.extension)
    }

}

class MissingBlogConfiguration: Exception("No blog configuration supplied")