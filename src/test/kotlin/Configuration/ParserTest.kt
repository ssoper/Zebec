package Configuration

import com.seansoper.zebec.Configuration.Parser
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File

class ParserTest: StringSpec({
    val path = this.javaClass.classLoader.getResource("zebec.config")!!.path

    "valid" {
        val parser = Parser(path, "/test/path")

        parser.configuration.source.toString().shouldBe("/test/path/src")
        parser.configuration.destination.toString().shouldBe("/test/path/dest")
        parser.configuration.port.shouldBe(9090)
        parser.configuration.extensions.contains("jpg").shouldBeTrue()
        parser.configuration.extensions.contains("pdf").shouldBeTrue()
        parser.configuration.extensions.count().shouldBe(2)
        parser.configuration.verbose.shouldBeTrue()
    }

})