abstract class Element(val type: String, val attributes: TagAttributes? = null) {
    abstract fun render(indent: Int = 0): String
    val tagAttributes: String = attributes?.map { " ${it.key}='${it.value}'" }?.joinToString(" ") ?: ""
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
        return  tag
    }

    fun tagWithText(type: String, text: String) {
        val tag = TagWithText(type, text)
        children += tag
    }
}

class TagWithText(type: String, val text: String, attributes: TagAttributes? = null): Element(type, attributes) {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        return "${indentation}<${type}>${text}</${type}>"
    }
}

class HTML: Tag("html") {
    fun head(init: Head.() -> Unit) = initTag(Head(), init)
    fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head: Tag("head") {
    fun title(text: String) = tagWithText("title", text)
}

class Body: Tag("body") {
    fun div(attributes: TagAttributes? = null, init: DivTag.() -> Unit) = initTag(DivTag(attributes), init)
}

class DivTag(attributes: TagAttributes? = null): Tag("div", attributes) {
    fun p(init: PTag.() -> Unit) = initTag(PTag(), init)
    fun p(text: String) = tagWithText("p", text)
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
                title("This is a web page title")
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
