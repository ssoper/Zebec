package com.seansoper.zebec.fileProcessor

import com.seansoper.zebec.Blog
import com.seansoper.zebec.InvalidUnsplashURL
import com.seansoper.zebec.UnsplashImage
import com.seansoper.zebec.relativeProtocol
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class BlogEntry(val blog: Blog, val source: Path, val verbose: Boolean = false): Processable {

    val createdDate: LocalDateTime?
        get() {
            return try {
                (Files.getAttribute(source, "creationTime") as FileTime).
                    toInstant().
                    atOffset(ZoneOffset.UTC).
                    toLocalDateTime()
            } catch (exception: IOException) {
                null
            }
        }

    // TODO: Combine image and imageURL
    data class Metadata(val author: String,
                        val title: String,
                        val tags: Array<String>,
                        val imageURL: URL?,
                        val image: UnsplashImage?,
                        val subtitle: String?,
                        val firstParagraph: String?) {

        fun html(createdDate: String): String {
            var result = "<h1 class='mt-4'>$title</h1>"
            subtitle?.let { result += "<h2 class='subtitle'>$subtitle</h2>" }

            result += """
                <div class='author'>
                  <img src='/images/avatar.jpg' />
                  <ul>
                    <li>$author</li>
                    <li>$createdDate</li>
                  </ul>
                </div>
            """.trimIndent()

            image?.let {
                result += "<img class='img-fluid rounded' src='${it.entryUrlNormal}' srcset='${it.entryUrlNormal} 1x, ${it.entryUrlRetina} 2x'>"
            } ?: imageURL?.let {
                result += "<img class='img-fluid rounded' src='${it.relativeProtocol}' />"
            }

            return result
        }
    }

    private val TitleRegex = "([a-z0-9]+(([’',. -][a-z0-9 ])?[a-z0-9]*)*)"
    private val Formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.US)

    override fun process(content: String): String? {
        val html = Markdown().process(content) ?: return null
        val metadata = parseMetadata(content) ?: return null
        val formattedDate = createdDate?.format(Formatter) ?: return null
        var trimmed = html.
            replace(Regex("</?body>"), "").
            replace("<pre><code>", "<pre><code>\n")
        trimmed = "${metadata.html(formattedDate)}$trimmed"

        return blog.template.render(trimmed)
    }

    fun parseMetadata(content: String): Metadata? {
        val author = parseAuthor(content)
        val title = parseTitle(content)

        return if (author != null && title != null) {
            val imageURL = parseImageURL(content)
            val image = imageURL?.let {
                try {
                    UnsplashImage(imageURL)
                } catch (exception: InvalidUnsplashURL) {
                    null
                }
            }

            Metadata(author,
                     title,
                     parseTags(content),
                     imageURL,
                     image,
                     parseSubtitle(content),
                     Markdown().getFirstParagraph((content)))
        } else {
            null
        }
    }

    fun getMetadata(): Metadata? {
        val content = File(source.toString()).readText()
        return parseMetadata(content)
    }

    private fun parseAuthor(content: String): String? {
        val regex = Regex("^\\[//]: # \\(zauthor: ([a-z]+(([’',. -][a-z ])?[a-z]*)*)\\)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
        return regex.find(content)?.groups?.get(1)?.value
    }

    private fun parseTitle(content: String): String? {
        val regex = Regex("^\\[//]: # \\(ztitle: $TitleRegex\\)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
        return regex.find(content)?.groups?.get(1)?.value
    }

    private fun parseSubtitle(content: String): String? {
        val regex = Regex("^\\[//]: # \\(zsubtitle: $TitleRegex\\)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
        return regex.find(content)?.groups?.get(1)?.value
    }

    private fun parseImageURL(content: String): URL? {
        val regex = Regex("^\\[//]: # \\(zimage: (https?://[^\\s/\$.?#].[^\\s]*)\\)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
        return regex.find(content)?.groups?.get(1)?.value.let { URL(it) }
    }

    private fun parseTags(content: String): Array<String> {
        val regex = Regex("^\\[//]: # \\(ztags: ([a-z]+(, ?[a-z]+)*)\\)$", setOf(RegexOption.MULTILINE, RegexOption.IGNORE_CASE))
        return regex.find(content)?.groups?.get(1)?.value?.split(", ?".toRegex())?.toTypedArray() ?: emptyArray()
    }
}