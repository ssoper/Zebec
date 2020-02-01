package configuration

import com.seansoper.zebec.CommandLineParser
import com.seansoper.zebec.configuration.Settings
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.nio.file.Paths

class SettingsTest: StringSpec({
    val path = Paths.get(this.javaClass.classLoader.getResource("zebec.config")!!.toURI())

    "valid" {
        val parsed = CommandLineParser.Parsed(path, true)
        val settings = Settings(parsed, "/test/path")

        settings.source.toString().shouldBe("/test/path/src")
        settings.destination.toString().shouldBe("/test/path/dest")
        settings.port.shouldBe(9090)
        settings.extensions.contains("jpg").shouldBeTrue()
        settings.extensions.contains("pdf").shouldBeTrue()
        settings.extensions.count().shouldBe(2)
        settings.verbose.shouldBeTrue()
        settings.blog!!.directory.toString().shouldBe("/test/path/blog")
        settings.blog!!.extension.shouldBe("md")
    }

})