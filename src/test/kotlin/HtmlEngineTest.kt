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

    "h2 tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    h2("Subtitle", mapOf("class" to "cover-heading"))
                }
            }
        }.render()
        result.shouldContain("<h2 class='cover-heading'>Subtitle</h2>")
    }

    "h3 tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    h3("Smaller title", mapOf("class" to "cover-heading"))
                }
            }
        }.render()
        result.shouldContain("<h3 class='cover-heading'>Smaller title</h3>")
    }

    "h4 tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    h4("Even smaller title", mapOf("class" to "cover-heading"))
                }
            }
        }.render()
        result.shouldContain("<h4 class='cover-heading'>Even smaller title</h4>")
    }

    "h5 tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    h5("Smallest title", mapOf("class" to "cover-heading"))
                }
            }
        }.render()
        result.shouldContain("<h5 class='cover-heading'>Smallest title</h5>")
    }

    "blockquote tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    blockquote("This is a blockquote")
                }
            }
        }.render()
        result.shouldContain("<blockquote>This is a blockquote</blockquote>")
    }

    "hr tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    hr()
                }
            }
        }.render()
        result.shouldContain("<hr />")
    }

    "comment tag" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    comment("This is a comment")
                }
            }
        }.render()
        result.shouldContain("<!-- This is a comment -->")
    }

    "raw html" {
        val result = HtmlEngine().html {
            body {
                div(mapOf("class" to "site-wrapper")) {
                    raw("<this><is>a</is></this>tag")
                }
            }
        }.render()
        result.shouldContain("<this><is>a</is></this>tag")
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