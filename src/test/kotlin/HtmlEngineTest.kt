import com.seansoper.zebec.HtmlEngine
import io.kotlintest.specs.StringSpec

class HtmlEngineTest: StringSpec({

    "generate simple html" {
        val titleStr = "This is the title"
        val result = HtmlEngine().html {
            head {
                title(titleStr)
            }
        }.render()
        assert(result.startsWith("<!doctype html>"))
        assert(result.contains("<title>$titleStr</title>"))
    }

})