package com.seansoper.zebec.fileProcessor

import com.seansoper.zebec.configuration.Settings
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class Markdown(val settings: Settings? = null): Processable {

    override fun process(content: String): String? {
        val flavor = CommonMarkFlavourDescriptor()
        val parser = MarkdownParser(flavor).buildMarkdownTreeFromString(content)
        return HtmlGenerator(content, parser, flavor).generateHtml()
    }

}