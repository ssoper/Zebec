package com.seansoper.zebec.fileProcessor

import com.seansoper.zebec.blog.Blog
import com.seansoper.zebec.blog.BlogEntryMetadata
import java.io.FileNotFoundException
import java.net.URL
import java.nio.file.Path

class BlogEntry(val blog: Blog,
                val source: Path,
                val relativeDestination: String? = null,
                val verbose: Boolean = false): Processable {

    val entryURL: URL?
        get() {
            val relativeDestination = relativeDestination ?: return null
            val host = blog.host ?: return null
            return URL("https", host, relativeDestination)
        }

    override fun process(content: String): String? {
        val metadata = BlogEntryMetadata.nullable(source, entryURL) ?: run {
            if (verbose) {
                println("Source file $source not found")
            }

            return null
        }

        val html = Markdown().process(content) ?: return null

        var trimmed = html.
            replace(Regex("</?body>"), "").
            replace("<pre><code>", "<pre><code>\n")
        trimmed = "${metadata.entryHtml}$trimmed"

        return blog.template.render(trimmed)
            .replace("<head>", "<head>\n${metadata.socialMediaMetaTags}")
            .replace(Regex("title\\>.+?\\<"), "title>${metadata.title} by ${metadata.author}<")

    }

}