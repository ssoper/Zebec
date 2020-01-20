package com.seansoper.zebec

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths

// break up EventProcessors into smaller processors
// add support for processing

class KTMLCompiler(val templateBasePath: String = System.getProperty("user.dir")) {
    fun main() {
        println("stuff")
    }
/*
    inner class Include(val path: String): HtmlEngine.Element("include") {
        override fun render(indent: Int): String {
            val indentation = " ".repeat(indent)
            val filePath = Paths.get(this@HtmlEngine.templateBasePath, path)
            return try {
                File(filePath.toString()).readText()
            } catch (exception: FileNotFoundException) {
                val msg = "ERROR: template file $filePath not found"
                println(msg)
                "$indentation<!-- $msg -->"
            }
        }
    }
    */

    // fun include(path: String) = Include(path)

    abstract class Element(val type: String, val attributes: TagAttributes? = null) {
        abstract fun render(indent: Int = 0): String
        val tagAttributes: String = attributes?.map { " ${it.key}='${it.value}'" }?.joinToString("") ?: ""
    }

    interface TagProvidable {
        fun addTag(tag: KTMLCompiler.Element)
    }

    open class Tag(type: String, attributes: TagAttributes? = null) : KTMLCompiler.Element(type, attributes) {
        var children: Array<KTMLCompiler.Element> = emptyArray()

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

        open fun addTag(tag: KTMLCompiler.Element) {
            children += tag
        }
    }

    class Head : KTMLCompiler.Tag("head") {
        /*
        fun title(text: String) = addTag(HtmlEngine.TagWithText("title", text))
        fun meta(attributes: TagAttributes) = addTag(HtmlEngine.TagSelfClosing("meta", attributes))
        fun comment(comment: String) = addTag(HtmlEngine.TagComment(comment))

        fun ifComment(condition: String, init: HtmlEngine.TagConditionalComment.() -> Unit) =
            initTag(HtmlEngine.TagConditionalComment(condition), init)

        fun link(relType: HtmlEngine.LinkRelType, attributes: TagAttributes) {
            linkTag(relType, attributes) {
                addTag(HtmlEngine.TagSelfClosing("link", it))
            }
        }
        */

    }

    class HTML(language: String) : KTMLCompiler.Tag("html", mapOf("lang" to language)), IncludeTagProvider<HTML> {

        override fun render(indent: Int): String {
            val content = super.render(indent)
            return "<!doctype html>\n$content"
        }

        fun head(init: KTMLCompiler.Head.() -> Unit) = initTag(KTMLCompiler.Head(), init)
        fun include(path: String) = include(path, this)
//        fun body(init: KTMLCompiler.Body.() -> Unit) = initTag(HtmlEngine.Body(), init)
    }

}

class IncludeTag(val path: String): KTMLCompiler.Element("include") {
    override fun render(indent: Int): String {
        val indentation = " ".repeat(indent)

        return try {
            File(path).readText()
        } catch (exception: FileNotFoundException) {
            val msg = "ERROR: template file $path not found"
            println(msg)
            "$indentation<!-- $msg -->"
        }
    }
}

interface IncludeTagProvider<T: KTMLCompiler.Tag> {
    fun include(path: String, obj: T) {
        val tag = IncludeTag(path)
        obj.addTag(tag)
    }
}

interface HTMLTagProvider {
    fun html(language: String = "en", init: KTMLCompiler.HTML.() -> Unit): KTMLCompiler.HTML {
        val result = KTMLCompiler.HTML(language)
        result.init()
        return result
    }
}

interface HeadTagProvider
interface BodyTagProvider
interface DivTagProvider
interface ParagraphTagProvider
interface AnchorTagProvider
interface UlTagProvider
interface AllTagProvider
interface RootTags: IncludeTagProvider<KTMLCompiler.HTML>, HeadTagProvider, BodyTagProvider {

}

