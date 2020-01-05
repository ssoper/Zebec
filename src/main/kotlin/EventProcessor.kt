package com.seansoper.zebec

import com.seansoper.zebec.Utilities.humanReadableByteCount
import com.yahoo.platform.yui.compressor.CssCompressor
import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class EventProcessor(val changed: WatchFile.ChangedFile, val source: Path, val dest: Path, val verbose: Boolean = false) {

    private data class ProcessedDirs(val dir: Path, val parentDir: File)
    private data class ProcessedFile(val content: String, val fullname: String)

    fun process(done: (Path?) -> Unit) {
        val path = processFile { filename, extension, content ->
            when (extension) {
                "ktml" -> processHtml(content)?.let {
                    ProcessedFile(it, "$filename.html")
                }
                "js", "css" -> processScript(content, extension)?.let {
                    ProcessedFile(it, "$filename.min.$extension")
                }
                else -> {
                    if (verbose) {
                        println("ERROR: Unsupported content type $extension")
                    }

                    null
                }
            }
        }

        done(path)
    }

    private object YuiCompressorReporter: ErrorReporter {
        private fun createMessage(message: String?, source: String?, line: Int, lineOffset: Int): String {
            var errorMessage = "YuiCompressor: ${message ?: "Error encountered"}"
            source?.let { errorMessage += " ($this)" }
            errorMessage += " line: $line/$lineOffset"

            return errorMessage
        }

        override fun warning(message: String?, source: String?, line: Int, lineSource: String?, lineOffset: Int) {
            println(createMessage(message, source, line, lineOffset))
        }

        override fun error(message: String?, source: String?, line: Int, lineSource: String?, lineOffset: Int) {
            println(createMessage(message, source, line, lineOffset))
        }

        override fun runtimeError(message: String?, source: String?, line: Int, lineSource: String?, lineOffset: Int): EvaluatorException {
            return EvaluatorException(createMessage(message, source, line, lineOffset))
        }
    }

    private object YuiOptions {
        val lineBreakPos = -1
        val munge = true
        val preserveAllSemiColons = false
        val disableOptimizations = false
    }

    private fun processScript(content: String, extension: String): String? {
        val tmpName = java.util.UUID.randomUUID()
        val pathname = "/tmp/$tmpName.min.$extension"

        return try {
            if (extension == "js") {
                processJavascript(content, pathname)
            } else {
                processStylesheet(content, pathname)
            }
        } catch (exception: Exception) {
            println("YuiCompressor: $exception")
            null
        } finally {
            Paths.get(pathname).apply {
                if (Files.exists(this)) {
                    Files.delete(this)
                }
            }
        }
    }

    private fun processJavascript(content: String, pathname: String): String {
        val compressor = JavaScriptCompressor(content.reader(), YuiCompressorReporter)
        val output = File(pathname)

        output.writer().use {
            compressor.compress(
                it,
                YuiOptions.lineBreakPos,
                YuiOptions.munge,
                verbose,
                YuiOptions.preserveAllSemiColons,
                YuiOptions.disableOptimizations
            )
        }

        return File(pathname).readText()
    }

    private fun processStylesheet(content: String, pathname: String): String {
        val compressor = CssCompressor(content.reader())
        val output = File(pathname)

        output.writer().use {
            compressor.compress(
                it,
                YuiOptions.lineBreakPos
            )
        }

        return File(pathname).readText()
    }

    private fun processHtml(content: String): String? {
        val engine = ScriptEngineManager().getEngineByExtension("kts")

        // Because bindings are wonky
        val updatedContent = "import com.seansoper.zebec.HtmlEngine\n$content".
            replace("html ", "HtmlEngine().html ").
            replace("LinkRelType", "HtmlEngine.LinkRelType")

        return try {
            val compiled = engine.eval(updatedContent) as HtmlEngine.HTML
            compiled.render()
        } catch (exception: ScriptException) {
            if (verbose) {
                println("ERROR: Code didn’t compile")
            }

            null
        }
    }

    private fun getDirectories(changedPath: Path, source: Path, dest: Path): ProcessedDirs? {
        val destDir = changedPath.toString().split(source.toString()).elementAtOrNull(1) ?: return null
        val dir = Paths.get(dest.toString(), destDir)
        val parentDir = File(dir.toString().replace("/./", "/")).parentFile

        return ProcessedDirs(dir, parentDir)
    }

    private fun processFile(transform: (filename: String, extension: String, content: String) -> ProcessedFile?): Path? {
        val (dir, parentDir) = getDirectories(changed.path, source, dest) ?: return null
        val filename = dir.fileName.toString().split(".").firstOrNull() ?: return null
        val content = File(changed.path.toString()).readText()
        val result = transform(filename, changed.extension, content) ?: return null

        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }

        val path = Paths.get(parentDir.toString(), result.fullname)

        if (verbose) {
            val origSize = humanReadableByteCount(content.length)
            val newSize = humanReadableByteCount(result.content.length)
            println("Compiled ${dir.fileName}, $origSize → $newSize")
        }

        Files.write(path, result.content.toByteArray())
        return path
    }
}