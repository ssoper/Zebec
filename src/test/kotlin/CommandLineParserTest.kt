import com.seansoper.zebec.CommandLineParser
import io.kotlintest.specs.StringSpec

class CommandLineParserTest: StringSpec({

    val source = "-source=/empty/path"

    "verbose is true" {
        val parser = CommandLineParser(arrayOf("-verbose=true", source))
        assert(parser.parse()!!.verbose)
    }

})