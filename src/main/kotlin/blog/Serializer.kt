package com.seansoper.zebec.blog

import com.ctc.wstx.api.WstxOutputProperties
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.seansoper.zebec.blog.rss.Channel
import com.seansoper.zebec.blog.rss.Entry
import com.seansoper.zebec.blog.rss.Feed
import com.seansoper.zebec.configuration.Settings
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.ZoneId
import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
import java.util.*

class DateEncoder: JsonSerializer<GregorianCalendar>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(value: GregorianCalendar?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.apply {
            gen?.writeString(this.toZonedDateTime().format(RFC_1123_DATE_TIME))
        }
    }
}

class Serializer(private val settings: Settings) {

    fun generateFeed(basePath: String = settings.basePath): Boolean {
        val blog = settings.blog ?: return false
        val host = settings.host ?: return false

        return writeFeed(basePath, host, blog)
    }

    // TODO: Split into generate vs. write for testability
    fun writeFeed(basePath: String, host: String, blog: Blog): Boolean {
        val entries = getEntries(blog) ?: return false
        val relPath = "/blog"
        val writePath = "$basePath/$relPath/rss.xml"

        // TODO: Add title and description to blog configuration
        // TODO: URL path should come from config, difference between src and blog.directory. Or change config so that blog source is configure.src + blog.directory
        val channel = Channel("eat. code. stonks.", URL("https://$host$relPath"), "Writing code for its own sake", items = entries)

        val feed = Feed(channel)
        val mapper = XmlMapper(JacksonXmlModule().apply {
            setDefaultUseWrapper(false)
        }).apply {
            enable(SerializationFeature.INDENT_OUTPUT)
        }

        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
        mapper.factory.xmlOutputFactory.setProperty(WstxOutputProperties.P_USE_DOUBLE_QUOTES_IN_XML_DECL, true)

        val module = SimpleModule()
        module.addSerializer(GregorianCalendar::class.java, DateEncoder())
        mapper.registerModule(module)
        mapper.registerModule(KotlinModule())

        val serialized = mapper.writeValueAsString(feed)
        return try {
            Files.write(Paths.get(writePath), serialized.toByteArray())
            true
        } catch (exception: Exception) {
            false
        }
    }

    fun getEntries(blog: Blog): List<Entry>? {
        val entries = blog.getEntries() ?: return null
        // TODO: shouldn’t have to do this twice, maybe make it non-nullable
        val host = settings.host ?: return null

        return entries.sortedBy { it.createdDate }.reversed().mapNotNull { metadata ->
            blog.getEntryPath(metadata, settings)?.let { relativePath(settings.source, it) }?.let {
                val url = URL("https://$host$it")
                val description = metadata.subtitle ?: metadata.firstParagraph ?: ""
                val zoned = metadata.createdDate.atZone(ZoneId.systemDefault())
                val publishedDate = GregorianCalendar.from(zoned)
                Entry(metadata.title, url, description, publishedDate)
            }
        }
    }

    // TODO: Consolidate with Blog.relativePath, EventHandler.processFile and EventHandler.destination
    private fun relativePath(source: Path, compiledBlogPath: Path): String {
        val prefix = source.toString().commonPrefixWith(compiledBlogPath.toString())
        val path = compiledBlogPath.toString().removePrefix(prefix).replace("/./", "/")

        return if (path.startsWith("/")) {
            path
        } else {
            "/$path"
        }
    }
}