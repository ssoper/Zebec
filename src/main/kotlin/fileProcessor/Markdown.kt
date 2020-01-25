package com.seansoper.zebec.fileProcessor

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class Markdown: Processable {

    data class Blog(val author: String, val title: String, val tags: Array<String>)

    override fun process(content: String): String? {
        val flavor = CommonMarkFlavourDescriptor()
        val parser = MarkdownParser(flavor).buildMarkdownTreeFromString(content)
        return HtmlGenerator(content, parser, flavor).generateHtml()
    }

    fun parseMetaData(content: String): Blog? {
        val author = parseAuthor(content)
        val title = parseTitle(content)

        return if (author != null && title != null) {
            Blog(author, title, parseTags(content))
        } else {
            null
        }
    }

    private fun parseAuthor(content: String): String? {
        val regex = Regex("^\\[//]: # \\(zauthor: ([a-z]+(([’',. -][a-z ])?[a-z]*)*)\\)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
        return regex.find(content)?.groups?.get(1)?.value
    }

    private fun parseTitle(content: String): String? {
        val regex = Regex("^\\[//]: # \\(ztitle: ([a-z0-9]+(([’',. -][a-z0-9 ])?[a-z0-9]*)*)\\)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
        return regex.find(content)?.groups?.get(1)?.value
    }

    private fun parseTags(content: String): Array<String> {
        val regex = Regex("^\\[//]: # \\(ztags: ([a-z]+(, ?[a-z]+)*)\\)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
        return regex.find(content)?.groups?.get(1)?.value?.split(", ?".toRegex())?.toTypedArray() ?: emptyArray()
    }
}