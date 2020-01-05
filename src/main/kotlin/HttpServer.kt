package com.seansoper.zebec

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.PrintWriter
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Path

class HttpServer(path: Path, val port: Int, val verbose: Boolean) {

    val basePath: String

    enum class ContentType(val type: String) {
        html("text/html"),
        css("text/css"),
        js("text/javascript"),
        unknown("application/octet-stream")
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

    private data class Response(val content: String, val contentType: String)

    private val handler = { it: HttpExchange ->
        parse(it.requestURI)?.let { (content, contentType) ->
            it.responseHeaders.add("Content-Type", contentType)
            it.sendResponseHeaders(200, 0)
            PrintWriter(it.responseBody).use { response ->
                response.println(content)
            }
        } ?: run {
            it.responseHeaders.add("Content-Type", "text/plain")
            it.sendResponseHeaders(404, 0)
            PrintWriter(it.responseBody).use { response ->
                response.println("404/Not Found")
            }
        }
    }

    private fun parse(requestURI: URI): Response? {
        val path = if (requestURI.path.endsWith("/")) {
            "$basePath${requestURI.path}index.html"
        } else {
           "$basePath${requestURI.path}"
        }

        val file = File(path)

        return if (file.exists()) {
            val contentType = getContentType(path.split(".").last())
            Response(file.readText(), contentType)
        } else {
            null
        }
    }

    private fun getContentType(extension: String): String {
        return try {
            ContentType.valueOf(extension).type
        } catch (exception: Exception) {
            ContentType.unknown.type
        }
    }
}
