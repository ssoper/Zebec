package FileProcessor

import com.seansoper.zebec.FileProcessor.HTML
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class HTMLTest: StringSpec({
    "valid html" {
        val source = """
            html {
                head {
                    title("This is the title")
                }
            }
        """.trimIndent()

        val expected = """
            <!doctype html>
            <html lang='en'>
              <head>
                <title>This is the title</title>
              </head>
            </html>
        """.trimIndent()

        val result = HTML(false).process(source)!!
        result.shouldBe(expected)
    }

    "invalid html" {
        val source = "html { invalid }"
        val result = HTML(false).process(source)
        result.shouldBeNull()
    }
})