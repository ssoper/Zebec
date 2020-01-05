package com.seansoper.zebec

typealias TagAttributes = Map<String, String>

class HtmlEngine {

    abstract class Element(val type: String, val attributes: TagAttributes? = null) {
        abstract fun render(indent: Int = 0): String
        val tagAttributes: String = attributes?.map { " ${it.key}='${it.value}'" }?.joinToString("") ?: ""
    }

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

    abstract class Tag(type: String, attributes: TagAttributes? = null) : Element(type, attributes) {
        var children: Array<Element> = emptyArray()

        override fun render(indent: Int): String {
            val indentation = " ".repeat(indent)
            var str = "$indentation<$type$tagAttributes>\n"
            str += children.joinToString("\n") { it.render(indent + 2) }
            str += "\n$indentation</$type>"

            return str
        }

        fun <T : Tag> initTag(tag: T, init: T.() -> Unit): T {
            tag.init()
            children += tag
            return tag
        }

        fun addTag(tag: Element) {
            children += tag
        }
    }

    class TagWithText(type: String, val text: String, attributes: TagAttributes? = null) : Element(type, attributes) {
        override fun render(indent: Int): String {
            val indentation = " ".repeat(indent)
            return "$indentation<$type$tagAttributes>$text</$type>"
        }
    }

    class TagSelfClosing(type: String, attributes: TagAttributes?) : Element(type, attributes) {
        override fun render(indent: Int): String {
            val indentation = " ".repeat(indent)
            return "$indentation<$type$tagAttributes />"
        }
    }

    class TagComment(val comment: String) : Element("comment") {
        override fun render(indent: Int): String {
            val indentation = " ".repeat(indent)
            return "$indentation<!-- $comment -->"
        }
    }

    class TagConditionalComment(val condition: String) : Tag("comment"), SupportsScriptTag {
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

    class HTML(language: String) : Tag("html", mapOf("lang" to language)) {

        override fun render(indent: Int): String {
            val content = super.render(indent)
            return "<!DOCTYPE html>\n$content"
        }

        fun head(init: Head.() -> Unit) = initTag(Head(), init)
        fun body(init: Body.() -> Unit) = initTag(Body(), init)
    }

    class Head : Tag("head"), SupportsLinkTag {
        fun title(text: String) = addTag(TagWithText("title", text))
        fun meta(attributes: TagAttributes) = addTag(TagSelfClosing("meta", attributes))
        fun comment(comment: String) = addTag(TagComment(comment))
        fun ifComment(condition: String, init: TagConditionalComment.() -> Unit) =
            initTag(TagConditionalComment(condition), init)

        fun link(relType: LinkRelType, attributes: TagAttributes) {
            linkTag(relType, attributes) {
                addTag(TagSelfClosing("link", it))
            }
        }
    }

    class Body : Tag("body"), SupportsScriptTag {
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

    class NoScriptTag(attributes: TagAttributes?) : Tag("noscript", attributes), SupportsLinkTag {
        fun link(relType: LinkRelType, attributes: TagAttributes) {
            linkTag(relType, attributes) {
                addTag(TagSelfClosing("link", it))
            }
        }
    }

    class DivTag(attributes: TagAttributes?) : Tag("div", attributes) {
        fun div(attributes: TagAttributes?, init: DivTag.() -> Unit) = initTag(DivTag(attributes), init)
        fun p(attributes: TagAttributes?, init: PTag.() -> Unit) = initTag(PTag(attributes), init)
        fun p(text: String, attributes: TagAttributes? = null) = addTag(TagWithText("p", text, attributes))
        fun h1(text: String, attributes: TagAttributes?) = addTag(TagWithText("h1", text, attributes))
        fun ul(attributes: TagAttributes? = null, init: UlTag.() -> Unit) = initTag(UlTag(attributes), init)
    }

    class UlTag(attributes: TagAttributes?) : Tag("ul", attributes) {
        fun li(attributes: TagAttributes? = null, init: LiTag.() -> Unit) = initTag(LiTag(attributes), init)
        fun li(text: String, attributes: TagAttributes? = null) = addTag(TagWithText("li", text, attributes))
    }

    class LiTag(attributes: TagAttributes?) : Tag("li", attributes), SupportsATag {
        fun a(text: String, href: String, attributes: TagAttributes? = null) {
            aTag(text, href, attributes) {
                addTag(TagWithText("a", text, it))
            }
        }
    }

    class PTag(attributes: TagAttributes?) : Tag("p", attributes), SupportsATag {
        fun a(text: String, href: String, attributes: TagAttributes? = null) {
            aTag(text, href, attributes) {
                addTag(TagWithText("a", text, it))
            }
        }
    }

    class GoogleAnalyticsTag(val site: String) : Element("script") {
        override fun render(indent: Int): String {
            val indentation = " ".repeat(indent)
            return "$indentation<script>\n" +
                    "$indentation(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\n" +
                    "$indentation(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\n" +
                    "${indentation}m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\n" +
                    "$indentation})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');\n" +
                    "${indentation}if (typeof(ga) === 'function') { ga('create', '$site', 'auto'); ga('send', 'pageview'); }\n" +
                    "$indentation</script>"
        }
    }

    fun html(language: String = "en", init: HTML.() -> Unit): HTML {
        val result = HTML(language)
        result.init()
        return result
    }
}
