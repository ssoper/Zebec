package FileProcessor

import com.seansoper.zebec.FileProcessor.ScriptProcessor
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ScriptProcessorTest: StringSpec({

    "stylesheet" {
        val source = """
            body {
              background-color: #fff
            }
        """.trimIndent()
        val expected = "body{background-color:#fff}"
        val result = ScriptProcessor(ScriptProcessor.Type.stylesheet, false).process(source)!!
        result.shouldBe(expected)
    }

    "javascript" {
        val source = """
            $(function() {
              document.querySelectorAll("pre code");
            });
        """.trimIndent()
        val expected = "\$(function(){document.querySelectorAll(\"pre code\")});"
        val result = ScriptProcessor(ScriptProcessor.Type.javascript, false).process(source)!!
        result.shouldBe(expected)
    }

})