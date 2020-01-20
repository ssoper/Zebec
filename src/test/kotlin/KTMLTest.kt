
import TestHelper.loadResource
import TestHelper.pathForResource
import com.seansoper.zebec.HTMLTagProvider
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.specs.StringSpec

class Document: HTMLTagProvider {
    fun main() {
        html {
            include("/tmp/test.html")
        }
    }
}

class KTMLTest: StringSpec({

    "include content" {
        val path = pathForResource("head.html")
        val substr = loadResource("head.html").readText()
        val result = Document().html {
            include(path)
        }.render()
        result.shouldStartWith("<!doctype html>\n<html lang='en'>")
        result.shouldEndWith("</html>")
        result.shouldContain(substr)
        println(result)
    }
})