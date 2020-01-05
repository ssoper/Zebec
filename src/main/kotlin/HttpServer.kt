package com.seansoper.zebec

import com.seansoper.zebec.Utilities.humanReadableByteCount
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Path

class HttpServer(path: Path, val port: Int, val verbose: Boolean) {

    val basePath: String

    enum class ContentType(val type: String) {
        html("text/html"),
        css("text/css"),
        js("text/javascript"),
        otf("font/otf"),
        jpg("application/jpeg"),
        png("application/png"),
        ico("image/vnd.microsoft.icon"),
        unknown("application/octet-stream");

        fun isBinary(): Boolean {
            return this in listOf(otf, jpg, png, ico)
        }
    }

    init {
        basePath = path.toString().replace(Regex("/?\\.?$"), "")
    }

    fun start() {
        if (verbose) {
            println("Serving $basePath at localhost:$port")
        }

        HttpServer.create(InetSocketAddress(port), 0).apply {
            createContext("/", handler)
            start()
        }
    }

    private data class Response(val file: File, val contentType: ContentType, val logger: RequestLogger?)

    private class RequestLogger(val path: String) {
        private var content: String = "Received request for $path"

        fun add(event: String) {
            content += "\n* $event"
        }

        fun close() {
            content += "\nFinished request for $path\n"
            println(content)
        }
    }

    private val handler = { it: HttpExchange ->
        val logger = if (verbose) {
            RequestLogger(it.requestURI.path)
        } else {
            null
        }

        it.responseHeaders.add("via", "zebec 1.0")

        parse(it.requestURI, logger)?.let { (file, contentType, logger) ->
            it.responseHeaders.add("Content-Type", contentType.type)
            it.sendResponseHeaders(200, 0)
            logger?.add("Content type: ${contentType.type}")

            if (contentType.isBinary()) {
                val bytes = file.readBytes()
                logger?.add("Size: ${humanReadableByteCount(bytes.size)} (binary)")
                it.responseBody.write(bytes)
                it.responseBody.close()
                logger?.close()
            } else {
                PrintWriter(it.responseBody).use { response ->
                    val text = file.readText()
                    logger?.add("Size: ${humanReadableByteCount(text.length)}")
                    response.println(text)
                    logger?.close()
                }
            }
        } ?: run {
            it.responseHeaders.add("Content-Type", "text/plain")
            it.sendResponseHeaders(404, 0)
            PrintWriter(it.responseBody).use { response ->
                response.println("404/Not Found")
            }
        }
    }

    private fun parse(requestURI: URI, logger: RequestLogger?): Response? {
        val path = if (requestURI.path.endsWith("/")) {
            "$basePath${requestURI.path}index.html"
        } else {
           "$basePath${requestURI.path}"
        }

        logger?.add("Path: $path")

        val file = File(path)

        return if (file.exists()) {
            val contentType = getContentType(path.split(".").last())
            Response(file, contentType, logger)
        } else {
            null
        }
    }

    private fun getContentType(extension: String): ContentType {
        return try {
            ContentType.valueOf(extension)
        } catch (exception: Exception) {
            ContentType.unknown
        }
    }

}
