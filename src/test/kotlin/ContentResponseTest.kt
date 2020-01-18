import com.seansoper.zebec.ContentResponse
import com.seansoper.zebec.ContentServer
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class ContentResponseTest: StringSpec({
    val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    fun loadResource(fileName: String): File = File(this.javaClass.classLoader.getResource(fileName)!!.toURI())

    // Credit: https://www.baeldung.com/kotlin-random-alphanumeric-string
    fun randomString(length: Int = 10): String  = (1..length)
        .map { kotlin.random.Random.nextInt(0, charPool.size) }
        .map(charPool::get)
        .joinToString("")

    "serve html" {
        val path = "/tmp/${randomString()}.html"
        val output = File(path)
        val input = loadResource("test.html")
        val response = ContentResponse(input, ContentServer.ContentType.html, null)
        response.serve(output.outputStream())

        val file = File(path)
        val text = file.readText()
        text.shouldContain(input.readText().substring(0..50))
        text.shouldContain("<script>const clientId=")
        file.delete()
    }
})