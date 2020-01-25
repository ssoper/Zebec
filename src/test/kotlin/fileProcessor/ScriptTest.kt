package fileProcessor

import com.seansoper.zebec.fileProcessor.Script
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class ScriptTest: StringSpec({

    "stylesheet" {
        val source = """
            body {
              background-color: #fff
            }
        """.trimIndent()
        val expected = "body{background-color:#fff}"
        val result = Script(Script.Type.stylesheet, false).process(source)!!
        result.shouldBe(expected)
    }

    "javascript" {
        val source = """
            $(function() {
              document.querySelectorAll("pre code");
            });
        """.trimIndent()
        val expected = "\$(function(){document.querySelectorAll(\"pre code\")});"
        val result = Script(Script.Type.javascript, false).process(source)!!
        result.shouldBe(expected)
    }

})