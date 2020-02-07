package fileProcessor

import com.seansoper.zebec.CommandLineParser
import com.seansoper.zebec.configuration.Settings
import com.seansoper.zebec.fileProcessor.BlogEntry
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class BlogEntryTest: StringSpec({
    val pathURI = this.javaClass.classLoader.getResource("zebec.config")!!.toURI()
    val path = Paths.get(pathURI)
    val basePath = File(pathURI).parentFile.toPath()
    val parsed = CommandLineParser.Parsed(path, true, false)
    val settings = Settings(parsed, basePath.toString())
    val sampleFile = Paths.get(this.javaClass.classLoader.getResource("src/blog/entry.md")!!.toURI())

    "valid" {
        val content = File(sampleFile.toString()).readText()
        val entry = BlogEntry(settings.blog!!, sampleFile).process(content)

        entry.shouldContain("<li>Sinead O’Connor St. Mark's</li>")
        entry.shouldContain("<h1>use kotlin</h1>")
        entry.shouldContain("<h2>it's great because</h2>")
        entry.shouldContain("<p>hello there</p>")
    }

})