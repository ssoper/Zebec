import java.io.File
import java.net.URL

object TestHelper {
    fun urlForResource(resource: String): URL = this.javaClass.classLoader.getResource(resource)!!
    fun pathForResource(resource: String): String = urlForResource(resource).path
    fun loadResource(resource: String): File = File(urlForResource(resource).toURI())
}