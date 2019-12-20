abstract class Element(val type: String, val attributes: TagAttributes? = null) {
    abstract fun render(indent: Int = 0): String
    val tagAttributes: String = attributes?.map { " ${it.key}='${it.value}'" }?.joinToString("") ?: ""
}

typealias TagAttributes = Map<String, String>

abstract class Tag(type: String, attributes: TagAttributes? = null): Element(type, attributes) {
    var children: Array<Element> = emptyArray()

    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        var str = "${indentation}<${type}${tagAttributes}>\n"
        str += children.map { it.render(indent + 2) }.joinToString("\n")
        str += "\n${indentation}</${type}>"

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
        return "${indentation}<${type}${tagAttributes}>${text}</${type}>"
    }
}

class TagSelfClosing(type: String, attributes: TagAttributes?): Element(type, attributes) {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        return "${indentation}<${type}${tagAttributes} />"
    }
}

class HTML: Tag("html") {
    fun head(init: Head.() -> Unit) = initTag(Head(), init)
    fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head: Tag("head") {
    fun title(text: String) = addTag(TagWithText("title", text))
    fun meta(attributes: TagAttributes?) = addTag(TagSelfClosing("meta", attributes))
    fun link(attributes: TagAttributes?) = addTag(TagSelfClosing("link", attributes))
}

class Body: Tag("body") {
    fun div(attributes: TagAttributes? = null, init: DivTag.() -> Unit) = initTag(DivTag(attributes), init)
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
