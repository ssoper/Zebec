package com.seansoper.zebec.fileProcessor

import com.seansoper.zebec.blog.Blog
import com.seansoper.zebec.blog.BlogEntryMetadata
import java.io.FileNotFoundException
import java.nio.file.Path

class BlogEntry(val blog: Blog, val source: Path, val verbose: Boolean = false): Processable {

    override fun process(content: String): String? {
        val html = Markdown().process(content) ?: return null
        val metadata = try {
            BlogEntryMetadata(source)
        } catch (_: FileNotFoundException) {
            if (verbose) {
                println("Source file $source not found")
            }

            null
        } ?: return null

        var trimmed = html.
            replace(Regex("</?body>"), "").
            replace("<pre><code>", "<pre><code>\n")
        trimmed = "${metadata.entryHtml}$trimmed"

        return blog.template.render(trimmed)
    }

}