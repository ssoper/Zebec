package com.seansoper.zebec.fileProcessor

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.findChildOfType
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class Markdown(): Processable {

    private val Flavor = CommonMarkFlavourDescriptor()

    override fun process(content: String): String? {
        val parser = MarkdownParser(Flavor).buildMarkdownTreeFromString(content)
        return HtmlGenerator(content, parser, Flavor).generateHtml()
    }

    // TODO: Update to account for bold, italics, etc in first paragraph
    // TODO: Add tests
    //       https://github.com/valich/intellij-markdown/blob/master/src/org/intellij/markdown/MarkdownElementTypes.kt
    fun getFirstParagraph(content: String): String? {
        val parser = MarkdownParser(Flavor).buildMarkdownTreeFromString(content)
        return parser.children.firstOrNull {
            it.type.toString() == "Markdown:PARAGRAPH"
        }?.let {
            var text = ""

            it.children.forEach {
                if (it.type == MarkdownElementTypes.INLINE_LINK) {
                    it.findChildOfType(MarkdownElementTypes.LINK_TEXT)?.let {
                        text += content.substring(it.startOffset+1, it.endOffset-1)
                    }
                } else {
                    text += content.substring(it.startOffset, it.endOffset)
                }
            }

            text.replace("\\*", "*")
        }
    }

}