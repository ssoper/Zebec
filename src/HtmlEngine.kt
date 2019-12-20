interface Element {
    fun render(indent: Int = 0): String
}

typealias TagAttributes = Map<String, String>

abstract class Tag(val type: String, val attributes: TagAttributes? = null): Element {
    var children: Array<Element> = emptyArray()

    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        val attrs = attributes?.map { " ${it.key}='${it.value}'" }?.joinToString(" ") ?: ""

        var str = "${indentation}<${type}${attrs}>\n"
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

class TagWithText(val type: String, val text: String): Element {
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
