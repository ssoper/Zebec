import com.seansoper.zebec.CommandLineParser
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class CommandLineParserTest: StringSpec({

    val source = "-source=/empty/path"

    "verbose is set to true" {
        val parser = CommandLineParser(arrayOf("-verbose=true", source))
        parser.parse()!!.verbose.shouldBeTrue()
    }

    "verbose is not set" {
        val parser = CommandLineParser(arrayOf(source))
        parser.parse()!!.verbose.shouldBeFalse()
    }

    "port is set" {
        val parser = CommandLineParser(arrayOf("-port=9000", source))
        parser.parse()!!.port.shouldBe(9000)
    }

    "port is not set" {
        val parser = CommandLineParser(arrayOf(source))
        parser.parse()!!.port.shouldBe(8080)
    }

    "source is set" {
        val parser = CommandLineParser(arrayOf(source))
        parser.parse()!!.source.toString().shouldEndWith("/empty/path")
    }

    "source is not set" {
        val parser = CommandLineParser(emptyArray())
        parser.parse().shouldBeNull()
    }

    "extension is set" {
        val parser = CommandLineParser(arrayOf("-extension=zip", source))
        parser.parse()!!.extensions.shouldContain("zip")
    }

    "extension is not set" {
        val parser = CommandLineParser(arrayOf(source))
        parser.parse()!!.extensions.shouldContain("css")
        parser.parse()!!.extensions.shouldContain("js")
        parser.parse()!!.extensions.shouldContain("ktml")
    }

})