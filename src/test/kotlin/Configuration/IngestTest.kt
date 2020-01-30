package configuration

import com.seansoper.zebec.configuration.Ingest
import com.seansoper.zebec.configuration.InvalidConfigurationException
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec

class IngestTest: StringSpec({

    "valid" {
        val config = Ingest("/test/path").configure {
            source = "src"
            destination = "dest"
            port = 9090
            extensions = arrayOf("txt", "html")
        }

        config.source.toString().shouldBe("/test/path/src")
        config.destination.toString().shouldBe("/test/path/dest")
        config.port.shouldBe(9090)
        config.extensions.contains("txt").shouldBeTrue()
        config.extensions.contains("html").shouldBeTrue()
        config.extensions.count().shouldBe(2)
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
    }

    "invalid" {
        shouldThrow<InvalidConfigurationException> {
            Ingest("/test/path").configure {
                port = 9090
            }
        }
    }
})