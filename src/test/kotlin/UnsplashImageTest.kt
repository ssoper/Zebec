import com.seansoper.zebec.blog.InvalidUnsplashURL
import com.seansoper.zebec.blog.UnsplashImage
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.net.URL

class UnsplashImageTest: StringSpec({

    "valid" {
        val url = "https://unsplash.com/photos/ZBWtD2Um_MzA"
        val result = UnsplashImage(URL(url))
        result.photoId.shouldBe("ZBWtD2Um_MzA")
        result.entryUrlNormal.shouldBe("//source.unsplash.com/ZBWtD2Um_MzA/900x300")
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