package com.seansoper.zebec.FileProcessor

import com.yahoo.platform.yui.compressor.CssCompressor
import com.yahoo.platform.yui.compressor.JavaScriptCompressor
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class Script(val type: Type, val verbose: Boolean): Processable {

    enum class Type(val extension: String) {
        javascript("js"),
        stylesheet("css")
    }

    override fun process(content: String): String? {
        val tmpName = java.util.UUID.randomUUID()
        val pathname = "/tmp/$tmpName.min.${type.extension}"

        return try {
            if (type == Type.javascript) {
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
}