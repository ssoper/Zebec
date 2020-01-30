package com.seansoper.zebec.fileProcessor

import com.seansoper.zebec.Utilities.humanReadableByteCount
import com.seansoper.zebec.WatchFile
import com.seansoper.zebec.configuration.Settings
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

interface Processable {
    fun process(content: String): String?
}

class EventHandler(val changed: WatchFile.ChangedFile, val settings: Settings) {

    private data class ProcessedDirs(val dir: Path, val parentDir: File)
    private data class ProcessedFile(val content: String, val fullname: String)

    fun process(done: (Path?) -> Unit) {
        val path = processFile { filename, extension, content ->
            when (extension) {
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

    private fun getDirectories(changedPath: Path, source: Path, dest: Path): ProcessedDirs? {
        val destDir = changedPath.toString().split(source.toString()).elementAtOrNull(1) ?: return null
        val dir = Paths.get(dest.toString(), destDir)
        val parentDir = File(dir.toString().replace("/./", "/")).parentFile

        return ProcessedDirs(dir, parentDir)
    }

    private fun processFile(transform: (filename: String, extension: String, content: String) -> ProcessedFile?): Path? {
        val (dir, parentDir) = getDirectories(changed.path, settings.source, settings.destination) ?: return null
        val filename = dir.fileName.toString().split(".").firstOrNull() ?: return null
        val content = File(changed.path.toString()).readText()
        val result = transform(filename, changed.extension, content) ?: return null

        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        val path = Paths.get(parentDir.toString(), result.fullname)

        if (settings.verbose) {
            val origSize = humanReadableByteCount(content.length)
            val newSize = humanReadableByteCount(result.content.length)
            println("Compiled ${dir.fileName}, $origSize → $newSize")
        }

        Files.write(path, result.content.toByteArray())
        return path
    }
}