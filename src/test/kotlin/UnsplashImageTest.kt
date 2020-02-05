import com.seansoper.zebec.InvalidUnsplashURL
import com.seansoper.zebec.UnsplashImage
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.net.URL

class UnsplashImageTest: StringSpec({

    "valid" {
        val url = "https://unsplash.com/photos/ZBWtD2UmMzA"
        val result = UnsplashImage(URL(url))
        result.photoId.shouldBe("ZBWtD2UmMzA")
        result.entryUrlNormal.shouldBe("//source.unsplash.com/ZBWtD2UmMzA/900x300")
    }

    "trailing slash" {
        val url = "https://unsplash.com/photos/ZBWtD2UmMzA/"
        val result = UnsplashImage(URL(url))
        result.photoId.shouldBe("ZBWtD2UmMzA")
    }

    "invalid photo id" {
        shouldThrow<InvalidUnsplashURL> {
            val url = "https://unsplash.com/index.html"
            UnsplashImage(URL(url))
        }
    }

    "invalid host" {
        shouldThrow<InvalidUnsplashURL> {
            val url = "https://myphotos.com/photos/ZBWtD2UmMzA"
            UnsplashImage(URL(url))
        }
    }

})