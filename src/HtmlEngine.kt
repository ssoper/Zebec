abstract class Tag(val type: String) {
    var children: Array<Tag> = emptyArray()
    var result = ""

    open fun render(): String {
        var str = "<${type}>"
        str += children.map { it.render() }.joinToString("\n")
        str += "</${type}>"

        return str
    }
}

class TagWithText(type: String, val text: String): Tag(type) {
    override fun render(): String {
        return "<${type}>${text}</${type}>"
    }
}

class HTML: Tag("html") {
    fun head(init: Head.() -> Unit): Head {
        val result = Head()
        result.init()
        children += result

        return result
    }
}

class Head: Tag("head") {
    fun title(value: String): TagWithText {
        val tag = TagWithText("title", value)
        children += tag

        return tag
    }
}

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
        }

    println(result.render())
}
