
abstract class Element(val type: String, val attributes: TagAttributes? = null) {
    abstract fun render(indent: Int = 0): String
    val tagAttributes: String = attributes?.map { " ${it.key}='${it.value}'" }?.joinToString("") ?: ""
}

typealias TagAttributes = Map<String, String>

interface SupportsATag {
    fun aTag(text: String, href: String, attributes: TagAttributes? = null, addTag: (TagAttributes) -> Unit) {
        attributes?.also {
            val finalAttrs = it.toMutableMap()
            finalAttrs["href"] = href
            addTag(finalAttrs)
        } ?: run {
            addTag(mapOf("href" to href))
        }
    }
}

enum class LinkRelType(val value: String) {
    Shortcut("shortcut icon"),
    Stylesheet("stylesheet")
}

interface SupportsLinkTag {
    fun linkTag(relType: LinkRelType, attributes: TagAttributes, addTag: (TagAttributes) -> Unit) {
        val finalAttrs = attributes.toMutableMap()
        finalAttrs["rel"] = relType.value
        addTag(finalAttrs)
    }
}

interface SupportsScriptTag {
    fun scriptTag(src: String, attributes: TagAttributes? = null, addTag: (TagAttributes) -> Unit) {
        attributes?.also {
            val finalAttrs = it.toMutableMap()
            finalAttrs["src"] = src
            addTag(finalAttrs)
        } ?: run {
            addTag(mapOf("src" to src))
        }
    }
}

abstract class Tag(type: String, attributes: TagAttributes? = null): Element(type, attributes) {
    var children: Array<Element> = emptyArray()

    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        var str = "$indentation<$type$tagAttributes>\n"
        str += children.joinToString("\n") { it.render(indent + 2) }
        str += "\n$indentation</$type>"

        return str
    }

    fun <T: Tag>initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children += tag
        return tag
    }

    fun addTag(tag: Element) {
        children += tag
    }
}

class TagWithText(type: String, val text: String, attributes: TagAttributes? = null): Element(type, attributes) {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        return "$indentation<$type$tagAttributes>$text</$type>"
    }
}

class TagSelfClosing(type: String, attributes: TagAttributes?): Element(type, attributes) {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        return "$indentation<$type$tagAttributes />"
    }
}

class TagComment(val comment: String): Element("comment") {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        return "$indentation<!-- $comment -->"
    }
}

class TagConditionalComment(val condition: String): Tag("comment"), SupportsScriptTag {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        var str = "$indentation<!--[if $condition]>\n"
        str += children.joinToString("\n") { it.render(indent + 2) }
        str += "\n$indentation<![endif]-->"

        return str
    }

    fun script(src: String) {
        scriptTag(src) {
            addTag(TagWithText("script", "", it))
        }
    }
}

class HTML(language: String): Tag("html", mapOf("lang" to language)) {

    override fun render(indent: Int): String {
        val content = super.render(indent)
        return "<!DOCTYPE html>\n$content"
    }

    fun head(init: Head.() -> Unit) = initTag(Head(), init)
    fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head: Tag("head"), SupportsLinkTag {
    fun title(text: String) = addTag(TagWithText("title", text))
    fun meta(attributes: TagAttributes) = addTag(TagSelfClosing("meta", attributes))
    fun comment(comment: String) = addTag(TagComment(comment))
    fun ifComment(condition: String, init: TagConditionalComment.() -> Unit) = initTag(TagConditionalComment(condition), init)
    fun link(relType: LinkRelType, attributes: TagAttributes) {
        linkTag(relType, attributes) {
            addTag(TagSelfClosing("link", it))
        }
    }
}

class Body: Tag("body"), SupportsScriptTag {
    fun div(attributes: TagAttributes?, init: DivTag.() -> Unit) = initTag(DivTag(attributes), init)
    fun comment(comment: String) = addTag(TagComment(comment))
    fun noscript(attributes: TagAttributes?, init: NoScriptTag.() -> Unit) = initTag(NoScriptTag(attributes), init)
    fun gaTag(site: String) = addTag(GoogleAnalyticsTag(site))
    fun script(src: String, attributes: TagAttributes? = null) {
        scriptTag(src, attributes) {
            addTag(TagWithText("script", "", it))
        }
    }
}

class NoScriptTag(attributes: TagAttributes?): Tag("noscript"), SupportsLinkTag {
    fun link(relType: LinkRelType, attributes: TagAttributes) {
        linkTag(relType, attributes) {
            addTag(TagSelfClosing("link", it))
        }
    }
}

class DivTag(attributes: TagAttributes?): Tag("div", attributes) {
    fun div(attributes: TagAttributes?, init: DivTag.() -> Unit) = initTag(DivTag(attributes), init)
    fun p(attributes: TagAttributes?, init: PTag.() -> Unit) = initTag(PTag(attributes), init)
    fun p(text: String, attributes: TagAttributes? = null) = addTag(TagWithText("p", text, attributes))
    fun h1(text: String, attributes: TagAttributes?) = addTag(TagWithText("h1", text, attributes))
    fun ul(attributes: TagAttributes? = null, init: UlTag.() -> Unit) = initTag(UlTag(attributes), init)
}

class UlTag(attributes: TagAttributes?): Tag("ul", attributes) {
    fun li(attributes: TagAttributes? = null, init: LiTag.() -> Unit) = initTag(LiTag(attributes), init)
    fun li(text: String, attributes: TagAttributes? = null) = addTag(TagWithText("li", text, attributes))
}

class LiTag(attributes: TagAttributes?): Tag("li", attributes), SupportsATag {
    fun a(text: String, href: String, attributes: TagAttributes? = null) {
        aTag(text, href, attributes) {
            addTag(TagWithText("a", text, it))
        }
    }
}

class PTag(attributes: TagAttributes?): Tag("p", attributes), SupportsATag {
    fun a(text: String, href: String, attributes: TagAttributes? = null) {
        aTag(text, href, attributes) {
            addTag(TagWithText("a", text, it))
        }
    }
}

class GoogleAnalyticsTag(val site: String): Element("script") {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        return "$indentation<script>\n" +
               "$indentation(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n" +
               "$indentation(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n" +
               "${indentation}m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n" +
               "$indentation})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');\n" +
               "${indentation}if (typeof(ga) === 'function') { ga('create', '$site', 'auto'); ga('send', 'pageview'); }\n"+
               "$indentation</script>"
    }
}

fun html(language: String = "en", init: HTML.() -> Unit): HTML {
    val result = HTML(language)
    result.init()
    return result
}

fun main() {
    val result =
        html {
            head {
                meta(mapOf("charset" to "utf-8"))
                meta(mapOf("http-equiv" to "X-UA-Compatible", "content" to "IE=edge"))
                meta(mapOf("name" to "viewport", "content" to "width=device-width, initial-scale=1"))
                meta(mapOf("name" to "ICBM", "content" to "39.0840, 77.1528"))
                title("Sean Soper / Developer")
                link(LinkRelType.Shortcut, mapOf("type" to "image/x-icon", "href" to "/favicon.ico"))
                comment("HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries")
                ifComment("lt IE 9") {
                    script("https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js")
                    script("https://oss.maxcdn.com/respond/1.4.2/respond.min.js")
                }
            }
            body {
                div(mapOf("class" to "site-wrapper")) {
                    div(mapOf("class" to "site-wrapper-inner")) {
                        div(mapOf("class" to "cover-container")) {
                            div(mapOf("class" to "inner cover")) {
                                h1("Leader, Developer<br />&amp; Technical Architect", mapOf("class" to "cover-heading"))
                                p(mapOf("class" to "load")) {
                                    a("Follow me on GitHub", "//github.com/ssoper", mapOf("class" to "btn btn-lg btn-primary"))
                                    a("Download my CV","files/cv_for_sean_soper.pdf", mapOf("class" to "btn btn-lg btn-primary"))
                                }
                            }
                            div(mapOf("class" to "mastfoot")) {
                                div(mapOf("class" to "inner")) {
                                    p("Connect")
                                    ul(mapOf("class" to "list-inline")) {
                                        li {
                                            a("Email", "mailto:sean.soper@gmail.com")
                                        }
                                        li {
                                            a("Mastodon", "//mastodon.technology/@ssoper")
                                        }
                                        li {
                                            a("LinkedIn", "//linkedin.com/in/seansoper")
                                        }
                                        li {
                                            a("Twitter", "//twitter.com/ssoper")
                                        }
                                        li {
                                            a("CocoaHeads DC", "//cocoaheadsdc.org")
                                        }
                                        li(mapOf("class" to "hidden", "id" to "untapped")) {
                                            a("Untappd", "//untappd.com/user/ssoper")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                comment("jQuery (necessary for Bootstrap's JavaScript plugins)")
                script("https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js")
                comment("Bootstrap")
                script("https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js", mapOf(
                    "integrity" to "sha384-0mSbJDEHialfmuBBQP6A4Qrprq5OVfW37PRR3j5ELqxss1yVqOtnepnHVP9aJ7xS",
                    "crossorigin" to "anonymous"))
                comment("Delay loading of some assets for Google PageSpeed optimizations")
                noscript(mapOf("id" to "deferred-styles")) {
                    link(LinkRelType.Stylesheet, mapOf(
                        "href" to "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css",
                        "integrity" to "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7",
                        "crossorigin" to "anonymous"))
                    link(LinkRelType.Stylesheet, mapOf("href" to "css/cover.min.css"))
                    link(LinkRelType.Stylesheet, mapOf("href" to "css/theme.min.css"))
                }
                script("js/main.js")
                comment("Google Analytics")
                gaTag("UA-616637-1")
            }
        }

    println(result.render())
}

