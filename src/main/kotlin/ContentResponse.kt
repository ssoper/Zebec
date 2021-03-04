package com.seansoper.zebec

import java.io.File
import java.io.OutputStream
import java.io.PrintWriter

class ContentResponse(val file: File, val contentType: ContentServer.ContentType, val logger: ContentServer.RequestLogger?) {

    val fileSize: Int by lazy {
        file.readBytes().size
    }

    // TODO: No one is using anything from here below. Remove, update tests and streamline
    fun serve(stream: OutputStream) {
        logger?.add("Content-Type: ${contentType.type}")

        if (contentType.isBinary()) {
            serveBinary(stream)
        } else {
            serveText(stream)
        }
    }

    private fun serveBinary(stream: OutputStream) {
        logger?.add("Size: ${Utilities.humanReadableByteCount(fileSize)} (binary)")
        stream.write(file.readBytes())
        stream.close()
        logger?.close()
    }

    private fun serveText(stream: OutputStream) {
        val text = getTextContent()
        logger?.add("Size: ${Utilities.humanReadableByteCount(text.length)}")

        PrintWriter(stream).use {
            it.println(text)
            logger?.close()
        }
    }

    private fun getTextContent(): String {
        return if (contentType == ContentServer.ContentType.html) {
            val content = file.readText()
            content.replace("</body>", "  <script>$script</script>\n  </body>")
        } else {
            file.readText()
        }
    }

    private val script = """
        const clientId=(Math.random().toString(36).substring(2,5)+Math.random().toString(36).substring(2,5)).toLowerCase(),sse=new EventSource("/sse/"+clientId),removeClient=function(e,n){fetch("/sse/"+e,{method:"DELETE"}).then(function(){n&&n()})};sse.onmessage=function(e){"refresh"==e.data&&removeClient(clientId,function(){location.reload(!0)})},window.addEventListener("beforeunload",function(){removeClient(clientId)});
    """.trimIndent()
}