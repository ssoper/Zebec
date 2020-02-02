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
    val Formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.US)

    val pathURI = this.javaClass.classLoader.getResource("zebec.config")!!.toURI()
    val path = Paths.get(pathURI)
    val basePath = File(pathURI).parentFile.toPath()
    val parsed = CommandLineParser.Parsed(path, true, false)
    val settings = Settings(parsed, basePath.toString())
    val sampleFile = Paths.get(this.javaClass.classLoader.getResource("src/blog/entry.md")!!.toURI())
    val now = Instant.now().atOffset(ZoneOffset.UTC).toLocalDateTime().format(Formatter)

    data class Defaults(val author: String,
                        val title: String,
                        val subtitle: String,
                        val image: String,
                        val tags: String)

    val defaults = Defaults(
        "Sinead O’Connor St. Mark's",
        "This is. A, title. it’s your's. Also 9",
        "This is the subtitle",
        "http://placekitten.com/900/300",
        "kotlin, programming,java")

    "incomlete metadata" {
        val source = """
            [//]: # (ztitle: This is the title)
            This is the **content**
        """.trimIndent()

        val entry = BlogEntry(settings.blog!!, sampleFile, true).process(source)
        entry.shouldBeNull()
    }

    "parse metadata" {
        val source = """
            [//]: # (zauthor: ${defaults.author})
            [//]: # (ztitle: ${defaults.title})
            [//]: # (zsubtitle: ${defaults.subtitle})
            [//]: # (zimage: ${defaults.image})
            [//]: # (ztags: ${defaults.tags})
            This is the **content**
        """.trimIndent()

        val entry = BlogEntry(settings.blog!!, sampleFile).parseMetadata(source)!!

        entry.author.shouldBe(defaults.author)
        entry.title.shouldBe(defaults.title)
        entry.imageURL!!.toString().shouldBe(defaults.image)
        entry.subtitle!!.shouldBe(defaults.subtitle)

        val strTags = defaults.tags.split(", ?".toRegex())
        strTags.count().shouldBe(3)
        strTags.forEach { entry.tags.contains(it).shouldBeTrue() }
    }

    "relative protocol" {
        val image = "https://images.com/kitty/?width=300"
        val source = """
            [//]: # (zauthor: ${defaults.author})
            [//]: # (ztitle: ${defaults.title})
            [//]: # (zsubtitle: ${defaults.subtitle})
            [//]: # (zimage: $image)
            [//]: # (ztags: ${defaults.tags})
            This is the **content**
        """.trimIndent()

        val entry = BlogEntry(settings.blog!!, sampleFile).parseMetadata(source)!!
        val html = entry.html(now).toString()
        html.shouldContain("src='//images.com/kitty/?width=300'")
    }
})