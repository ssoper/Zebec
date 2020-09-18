import com.seansoper.zebec.CommandLineParser
import com.seansoper.zebec.ConfigFileNotFound
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths
import kotlin.script.experimental.jvm.impl.getResourcePathForClass

class CommandLineParserTest: StringSpec({

    val path = this.javaClass.classLoader.getResource("zebec.config")!!.path
    val source = "-config=$path"

    "verbose is set to true" {
        println(source)
        val parser = CommandLineParser(arrayOf("-verbose", source))
        parser.parse().verbose.shouldBeTrue()
    }

    "verbose is not set" {
        val parser = CommandLineParser(arrayOf(source))
        parser.parse().verbose.shouldBeFalse()
    }

    "config path is set" {
        val parser = CommandLineParser(arrayOf(source))
        parser.parse().pathToConfigFile.toString().shouldEndWith("zebec.config")
    }

    "config file doesn’t exist" {
        shouldThrow<ConfigFileNotFound> {
            val parser = CommandLineParser(emptyArray())
            parser.parse().pathToConfigFile.toString().shouldEndWith("zebec.config")
        }
    }

})