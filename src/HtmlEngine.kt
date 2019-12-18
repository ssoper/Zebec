interface Element {
    fun render(): String
}

abstract class Tag(val type: String): Element {
    var children: Array<Element> = emptyArray()

    override fun render(): String {
        var str = "<${type}>"
        str += children.map { it.render() }.joinToString("\n")
        str += "</${type}>"

        return str
    }

    fun <T: Tag>initTag(tag: T, init: T.() -> Unit): T {
        tag.init()
        children += tag

        return  tag
    }

    fun tagWithText(tag: TagWithText) {
        children += tag
    }
}

class TagWithText(val type: String, val text: String): Element {
    override fun render(): String {
        return "<${type}>${text}</${type}>"
    }
}

class HTML: Tag("html") {
    fun head(init: Head.() -> Unit) = initTag(Head(), init)
    fun body(init: Body.() -> Unit) = initTag(Body(), init)
}

class Head: Tag("head") {
    fun title(value: String): TagWithText {
        val tag = TagWithText("title", value)
        children += tag

        return tag
    }
}

class Body: Tag("body") {
    fun div(init: DivTag.() -> Unit) = initTag(DivTag(), init)
}

class DivTag: Tag("div") {
    fun p(init: PTag.() -> Unit) = initTag(PTag(), init)
    fun p(text: String) = TagWithText("p", text)
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
                    p("This is a paragraph")
                }
            }
        }

    println(result.render())
}
