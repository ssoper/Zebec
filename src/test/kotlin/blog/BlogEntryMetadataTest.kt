package fileProcessor

import com.seansoper.zebec.blog.BlogEntryMetadata
import com.seansoper.zebec.blog.InvalidAuthorException
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.io.FileNotFoundException
import java.nio.file.Paths

class BlogEntryMetadataTest: StringSpec({
    val sampleFile = Paths.get(this.javaClass.classLoader.getResource("src/blog/entry.md")!!.toURI())
    val noAuthorFile = Paths.get(this.javaClass.classLoader.getResource("src/blog/no_author.md")!!.toURI())

    "invalid file" {
        shouldThrow<FileNotFoundException> {
            BlogEntryMetadata(Paths.get("invalid", "path"))
        }
    }

    "missing metadata" {
        shouldThrow<InvalidAuthorException> {
            BlogEntryMetadata(noAuthorFile)
        }
    }

    "valid" {
        val entry = BlogEntryMetadata(sampleFile)

        entry.author.shouldContain("Sinead")
        entry.title.shouldContain("This is")
        entry.image!!.imageURL.toString().shouldContain("placekitten.com")
    }

})