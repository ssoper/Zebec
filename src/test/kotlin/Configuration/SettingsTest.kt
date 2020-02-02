package configuration

import com.seansoper.zebec.CommandLineParser
import com.seansoper.zebec.configuration.Settings
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File
import java.nio.file.Paths

class SettingsTest: StringSpec({
    val pathURI = this.javaClass.classLoader.getResource("zebec.config")!!.toURI()
    val path = Paths.get(pathURI)
    val basePath = File(pathURI).parentFile.toPath()

    "valid" {
        val parsed = CommandLineParser.Parsed(path, true)
        val settings = Settings(parsed, basePath.toString())

        settings.source.toString().shouldEndWith("/src")
        settings.destination.toString().shouldEndWith("/dest")
        settings.port.shouldBe(9090)
        settings.extensions.contains("jpg").shouldBeTrue()
        settings.extensions.contains("pdf").shouldBeTrue()
        settings.extensions.count().shouldBe(2)
        settings.verbose.shouldBeTrue()
        settings.blog!!.directory.toString().shouldEndWith("/blog")
        settings.blog!!.extension.shouldBe("md")
    }

})