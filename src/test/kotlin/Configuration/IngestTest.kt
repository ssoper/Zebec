import com.seansoper.zebec.Configuration.Ingest
import com.seansoper.zebec.Configuration.InvalidConfigurationException
import io.kotlintest.*
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.specs.StringSpec

class IngestTest: StringSpec({

    "valid" {
        val config = Ingest("/test/path").configure {
            source = "src"
            destination = "dest"
            port = 9090
            extensions = arrayOf("txt", "html")
            verbose = true
        }
        config.source.toString().shouldBe("/test/path/src")
        config.destination.toString().shouldBe("/test/path/dest")
        config.port.shouldBe(9090)
        config.extensions.contains("txt").shouldBeTrue()
        config.extensions.contains("html").shouldBeTrue()
        config.extensions.count().shouldBe(2)
        config.verbose.shouldBeTrue()
    }

    "default values" {
        val config = Ingest("/test/path").configure {
            source = "src"
        }
        config.source.toString().shouldBe("/test/path/src")
        config.destination.toString().shouldBe("/test/path/.")
        config.port.shouldBe(8080)
        config.extensions.contains("css").shouldBeTrue()
        config.extensions.contains("js").shouldBeTrue()
        config.extensions.count().shouldBe(4)
        config.verbose.shouldBeFalse()
    }

    "invalid" {
        shouldThrow<InvalidConfigurationException> {
            Ingest("/test/path").configure {
                port = 9090
            }
        }
    }
})