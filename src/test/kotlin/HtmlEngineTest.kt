import com.seansoper.zebec.HtmlEngine
import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.matchers.string.shouldEndWith
import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.should
import io.kotlintest.specs.StringSpec

class HtmlEngineTest: StringSpec({

    "title tag" {
        val titleStr = "This is the title"
        val result = HtmlEngine().html {
            head {
                title(titleStr)
            }
        }.render()
        result.shouldStartWith("<!doctype html>")
        result.shouldEndWith("</html>")
        result.shouldContain("<head>")
        result.shouldContain("</head>")
        result.shouldContain("<title>$titleStr</title>")
    }

    "meta tag" {
        val result = HtmlEngine().html {
            head {
                meta(mapOf("charset" to "utf-8"))
            }
        }.render()
        result.shouldContain("<meta charset='utf-8' />")
    }

    "link tag" {
        val result = HtmlEngine().html {
            head {
                link(HtmlEngine.LinkRelType.Shortcut, mapOf("type" to "image/x-icon", "href" to "/favicon.ico"))
            }
        }.render()
        result.shouldContain("<link type='image/x-icon' href='/favicon.ico' rel='shortcut icon' />")
    }

    "h1 tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    h1("Page Title", mapOf("class" to "cover-heading"))
                }
            }
        }.render()
        result.shouldContain("<body>")
        result.shouldContain("</body>")
        result.shouldContain("<div class='site-wrapper'>")
        result.shouldContain("</div>")
        result.shouldContain("<h1 class='cover-heading'>Page Title</h1>")
    }

    "anchor tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    ul {
                        li {
                            a("Google", "https://google.com")
                        }
                    }
                    p("Hello world")
                    p(mapOf("class" to "hidden")) {
                        a("Yahoo", "https://yahoo.com")
                    }
                }
            }
        }.render()
        result.shouldContain("<ul>")
        result.shouldContain("</ul>")
        result.shouldContain("<li>")
        result.shouldContain("</li>")
        result.shouldContain("<a href='https://google.com'>Google</a>")
        result.shouldContain("<p>Hello world</p>")
        result.shouldContain("<a href='https://yahoo.com'>Yahoo</a>")
    }

    "script tag" {
        val result = HtmlEngine().html {
            body {
                script("https://code.jquery.com/jquery.min.js", mapOf(
                    "integrity" to "sha",
                    "crossorigin" to "anonymous"))
            }
        }.render()
        result.shouldContain("<script ")
        result.shouldContain("</script>")
        result.shouldContain("src='https://code.jquery.com/jquery.min.js'")
        result.shouldContain("integrity='sha'")
        result.shouldContain("crossorigin='anonymous'")
    }

    "google analytics tag" {
        val result = HtmlEngine().html {
            body {
                gaTag("SITE ID")
            }
        }.render()
        result.shouldContain("ga('create', 'SITE ID', 'auto')")
    }
})