package fileProcessor

import com.seansoper.zebec.Blog
import com.seansoper.zebec.CommandLineParser
import com.seansoper.zebec.configuration.Settings
import com.seansoper.zebec.fileProcessor.BlogEntry
import com.seansoper.zebec.fileProcessor.Markdown
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import java.io.File
import java.nio.file.Paths

class BlogEntryTest: StringSpec({
    val pathURI = this.javaClass.classLoader.getResource("zebec.config")!!.toURI()
    val path = Paths.get(pathURI)
    val basePath = File(pathURI).parentFile.toPath()
    val parsed = CommandLineParser.Parsed(path, true)
    val settings = Settings(parsed, basePath.toString())
    val sampleFile = Paths.get(this.javaClass.classLoader.getResource("src/blog/entry.md")!!.toURI())

    "incomlete metadata" {
        val source = """
            [//]: # (ztitle: This is the title)
            This is the **content**
        """.trimIndent()

        val entry = BlogEntry(settings.blog!!, sampleFile, true).process(source)
        entry.shouldBeNull()
    }

    "parse metadata" {
        val author = "Sinead O’Connor St. Mark's"
        val title = "This is. A, title. it’s your's. Also 9"
        val subtitle = "This is the subtitle"
        val image = "http://placekitten.com/900/300"
        val tags = "kotlin, programming,java"
        val source = """
            [//]: # (zauthor: $author)
            [//]: # (ztitle: $title)
            [//]: # (zsubtitle: $subtitle)
            [//]: # (zimage: $image)
            [//]: # (ztags: $tags)
            This is the **content**
        """.trimIndent()

        val entry = BlogEntry(settings.blog!!, sampleFile).parseMetadata(source)!!

        entry.author.shouldBe(author)
        entry.title.shouldBe(title)
        entry.imageURL!!.toString().shouldBe(image)
        entry.subtitle!!.shouldBe(subtitle)

        val strTags = tags.split(", ?".toRegex())
        strTags.count().shouldBe(3)
        strTags.forEach { entry.tags.contains(it).shouldBeTrue() }
    }

})