package com.seansoper.zebec.FileProcessor

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class Markdown: Processable {

    override fun process(content: String): String? {
        val flavor = CommonMarkFlavourDescriptor()
        val parser = MarkdownParser(flavor).buildMarkdownTreeFromString(content)
        return HtmlGenerator(content, parser, flavor).generateHtml()
    }

}