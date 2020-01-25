package FileProcessor

import com.seansoper.zebec.FileProcessor.Markdown
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MarkdownTest: StringSpec({
    "simple markdown" {
        val source = "This is *italicized*"
        val expected = "<body><p>This is <em>italicized</em></p></body>"
        val result = Markdown().process(source)!!
        result.shouldBe(expected)
    }

    "configuration" {
        val source = """
            [//]: # (ztitle: This is the title)
            This is the **content**
        """.trimIndent()
        val expected = "<body><p>This is the <strong>content</strong></p></body>"
        val result = Markdown().process(source)!!
        result.shouldBe(expected)
    }
})