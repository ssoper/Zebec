import com.sun.net.httpserver.HttpServer
import java.io.PrintWriter
import java.net.InetSocketAddress

fun main() {
    HttpServer.create(InetSocketAddress(8080), 0).apply {
        createContext("/") {
            it.responseHeaders.add("content-type", "text/html")
            it.sendResponseHeaders(200, 0)
            PrintWriter(it.responseBody).use { response ->
                response.println("<html><body>Hello!</body></html>")
            }
        }

        start()
    }
}