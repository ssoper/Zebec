package fileProcessor

import com.seansoper.zebec.fileProcessor.Markdown
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MarkdownTest: StringSpec({

    "simple markdown" {
        val source = "This is *italicized*"
        val expected = "<body><p>This is <em>italicized</em></p></body>"
        val result = Markdown().process(source)!!
        result.shouldBe(expected)
    }

})