import com.seansoper.zebec.Utilities
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class UtilitiesTest: StringSpec({

    "bytes" {
        val result = Utilities.humanReadableByteCount(10)
        result.endsWith("bytes")
    }

    "kilobytes" {
        val result = Utilities.humanReadableByteCount(10240)
        result.endsWith("KB")
    }

    "megabytes" {
        val result = Utilities.humanReadableByteCount(10240000)
        result.endsWith("MB")
    }

})