package com.seansoper.zebec

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.URI
import java.nio.file.Path

class ContentServer(path: Path, val port: Int, val verbose: Boolean) {

    val basePath: String

    enum class ContentType(val type: String) {
        html("text/html"),
        css("text/css"),
        js("text/javascript"),
        otf("font/otf"),
        jpg("image/jpeg"),
        png("image/png"),
        webp("image/webp"),
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
        println("Serving $basePath at localhost:$port")

        HttpServer.create(InetSocketAddress(port), 0).apply {
            createContext("/", handler)
            createContext("/sse", sse)
            start()
        }
    }

    class RequestLogger(val path: String) {
        private var content: String = "Received request for $path"

        fun add(event: String) {
            content += "\n* $event"
        }

        fun close(consumer: ((String) -> Unit)? = null) {
            content += "\nFinished request for $path\n"

            consumer?.let {
                it(content)
            } ?: run {
                println(content)
            }
        }
    }

    private val clients: MutableMap<String, HttpExchange> = mutableMapOf()

    fun refresh() {
        if (verbose) {
            println("Refresh event received, updating ${clients.count()} clients")
        }

        clients.forEach { (_, context) ->
            PrintWriter(context.responseBody).use {
                it.print("data: refresh\n\n")
            }
        }
    }

    private fun getClientId(path: String): String? {
        val regex = Regex("/([0-9]|[a-z]){6}\$")
        return regex.find(path)?.value?.removePrefix("/")
    }

    private val sse = fun(context: HttpExchange) {
        val clientId = getClientId(context.requestURI.path) ?: return

        if (context.requestMethod == "DELETE") {
            if (verbose) {
                println("Client $clientId disconnected")
            }

            clients.remove(clientId)

            context.responseHeaders.add("Content-Type", "text/plain")
            context.sendResponseHeaders(200, 0)
            PrintWriter(context.responseBody).use {
                it.print("Removed client")
            }

            return
        }

        if (verbose) {
            println("Client $clientId connected via SSE")
        }

        clients[clientId] = context

        context.responseHeaders.add("Content-Type", "text/event-stream")
        context.responseHeaders.add("Cache-Control", "no-cache")
        context.responseHeaders.add("Connection", "Keep-Alive")
        context.responseHeaders.add("Keep-Alive", "timeout=600")
        context.sendResponseHeaders(200, 0)
    }

    private val handler = fun(it: HttpExchange) {
        val logger = if (verbose) {
            RequestLogger(it.requestURI.path)
        } else {
            null
        }

        it.responseHeaders.add("Cache-Control", "no-cache")
        it.responseHeaders.add("via", "zebec 1.0")

        parse(it.requestURI, logger)?.let { response ->
            it.responseHeaders.add("Content-Type", response.contentType.type)
            it.responseHeaders.add("Accept-Ranges", "bytes")
            it.sendResponseHeaders(200, response.fileSize.toLong())

            logger?.add("Size: ${Utilities.humanReadableByteCount(response.fileSize)} (binary)")
            it.responseBody.write(response.file.readBytes())
            it.responseBody.close()
            logger?.close()

            it.requestBody.close()
        } ?: run {
            it.responseHeaders.add("Content-Type", "text/plain")
            it.sendResponseHeaders(404, 0)
            PrintWriter(it.responseBody).use { response ->
                response.println("404/Not Found")
            }
        }
    }

    private fun isDirectory(requestURI: URI): Boolean {
        val lastComponent = requestURI.path.split("/").lastOrNull()?.split(".") ?: return false
        return lastComponent.count() == 1
    }

    private fun parse(requestURI: URI, logger: RequestLogger?): ContentResponse? {
        val path = if (requestURI.path.endsWith("/")) {
            "$basePath${requestURI.path}index.html"
        } else if (isDirectory(requestURI)) {
            "$basePath${requestURI.path}/index.html"
        } else {
            "$basePath${requestURI.path}"
        }

        logger?.add("Path: $path")

        val file = File(path)

        return if (file.exists()) {
            val contentType = getContentType(path.split(".").last())
            ContentResponse(file, contentType, logger)
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
