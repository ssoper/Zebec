package com.seansoper.zebec.fileProcessor

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class Markdown(): Processable {

    private val Flavor = CommonMarkFlavourDescriptor()

    override fun process(content: String): String? {
        val parser = MarkdownParser(Flavor).buildMarkdownTreeFromString(content)
        return HtmlGenerator(content, parser, Flavor).generateHtml()
    }

    fun getFirstParagraph(content: String): String? {
        val parser = MarkdownParser(Flavor).buildMarkdownTreeFromString(content)
        return parser.children.firstOrNull {
            it.type.toString() == "Markdown:PARAGRAPH"
        }?.let {
            content.substring(it.startOffset, it.endOffset)
        }
    }

}