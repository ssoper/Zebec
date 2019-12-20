interface Element {
    fun render(indent: Int = 0): String
}

abstract class Tag(val type: String): Element {
    var children: Array<Element> = emptyArray()

    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)
        var str = "${indentation}<${type}>\n"
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
    fun div(init: DivTag.() -> Unit) = initTag(DivTag(), init)
}

class DivTag: Tag("div") {
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
                div {
                    p {

                    }
                    p("This is a paragraph")
                }
            }
        }

    println(result.render())
}
