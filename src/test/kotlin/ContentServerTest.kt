import com.seansoper.zebec.ContentServer
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class ContentServerTest: StringSpec({

    "html content type" {
        val result = ContentServer.ContentType.valueOf("html")
        result.shouldBe(ContentServer.ContentType.html)
        result.type.shouldBe("text/html")
    }

    "jpg content type" {
        val result = ContentServer.ContentType.valueOf("jpg")
        result.shouldBe(ContentServer.ContentType.jpg)
        result.type.shouldBe("application/jpeg")
        result.isBinary().shouldBeTrue()
    }

    "unknown content type" {
        shouldThrow<IllegalArgumentException> {
            ContentServer.ContentType.valueOf("fake")
        }

        ContentServer.ContentType.unknown.type.shouldBe("application/octet-stream")
    }

    "request logger" {
        val path = "/empty/path"
        val event = "This is the first event"
        val logger = ContentServer.RequestLogger(path)
        logger.add(event)
        logger.close {
            it.shouldContain("* $event")
            it.shouldEndWith("$path\n")
        }
    }
})
