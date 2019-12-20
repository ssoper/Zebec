abstract class Element(val type: String, val attributes: TagAttributes? = null) {
    abstract fun render(indent: Int = 0): String
    val tagAttributes: String = attributes?.map { " ${it.key}='${it.value}'" }?.joinToString("") ?: ""
}

typealias TagAttributes = Map<String, String>

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

class TagConditionalComment(val condition: String): Tag("comment") {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        var str = "$indentation<!--[if $condition]>\n"
        str += children.joinToString("\n") { it.render(indent + 2) }
        str += "\n$indentation<![endif]-->"

        return str
    }

    fun script(src: String) = addTag(TagWithText("script", "", mapOf("src" to src)))
}

class HTML: Tag("html") {
    fun head(init: Head.() -> Unit) = initTag(Head(), init)
    fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head: Tag("head") {
    fun title(text: String) = addTag(TagWithText("title", text))
    fun meta(attributes: TagAttributes?) = addTag(TagSelfClosing("meta", attributes))
    fun link(attributes: TagAttributes?) = addTag(TagSelfClosing("link", attributes))
    fun comment(comment: String) = addTag(TagComment(comment))
    fun ifComment(condition: String, init: TagConditionalComment.() -> Unit) = initTag(TagConditionalComment(condition), init)
}

class Body: Tag("body") {
    fun div(attributes: TagAttributes? = null, init: DivTag.() -> Unit) = initTag(DivTag(attributes), init)
    fun comment(comment: String) = addTag(TagComment(comment))
}

class DivTag(attributes: TagAttributes? = null): Tag("div", attributes) {
    fun p(init: PTag.() -> Unit) = initTag(PTag(), init)
    fun p(text: String) = addTag(TagWithText("p", text))
}

class PTag: Tag("p")

fun html(init: HTML.() -> Unit): HTML {
    val result = HTML()
    result.init()
    return result
}

fun main(args: Array<String>) {
    val result =
        html {
            head {
                meta(mapOf("charset" to "utf-8"))
                meta(mapOf("http-equiv" to "X-UA-Compatible", "content" to "IE=edge"))
                meta(mapOf("viewport" to "width=device-width, initial-scale=1"))
                meta(mapOf("ICBM" to "39.0840, 77.1528"))
                title("Sean Soper / Developer")
                link(mapOf("rel" to "shortcut icon", "type" to "image/x-icon", "href" to "/favicon.ico"))
                comment("HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries")
                ifComment("lt IE 9") {
                    script("https://oss.maxcdn.com/html5shiv/3.7.2/html5shiv.min.js")
                    script("https://oss.maxcdn.com/respond/1.4.2/respond.min.js")
                }
            }
            body {
                div(mapOf("class" to "site-wrapper")) {
                    p {

                    }
                    p("This is a paragraph")
                }
            }
        }

    println(result.render())
}
