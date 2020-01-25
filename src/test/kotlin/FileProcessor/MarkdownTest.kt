package FileProcessor

import com.seansoper.zebec.FileProcessor.Markdown
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class MarkdownTest: StringSpec({

    "simple markdown" {
        val source = "This is *italicized*"
        val expected = "<body><p>This is <em>italicized</em></p></body>"
        val result = Markdown().process(source)!!
        result.shouldBe(expected)
    }

    "metadata not in html" {
        val source = """
            [//]: # (ztitle: This is the title)
            This is the **content**
        """.trimIndent()
        val expected = "<body><p>This is the <strong>content</strong></p></body>"
        val result = Markdown().process(source)!!
        result.shouldBe(expected)
    }

    "parse metadata" {
        val author = "Sinead O’Connor St. Mark's"
        val title = "This is. A, title. it’s your's. Also 9"
        val tags = "kotlin, programming,java"
        val source = """
            [//]: # (zauthor: $author)
            [//]: # (ztitle: $title)
            [//]: # (ztags: $tags)
            This is the **content**
        """.trimIndent()

        val blog = Markdown().parseMetaData(source)!!
        blog.author.shouldBe(author)
        blog.title.shouldBe(title)
        val strTags = tags.split(", ?".toRegex())
        strTags.count().shouldBe(3)
        strTags.forEach { blog.tags.contains(it).shouldBeTrue() }
    }
})