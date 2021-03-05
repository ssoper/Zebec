package com.seansoper.zebec.blog

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.seansoper.zebec.blog.rss.Channel
import com.seansoper.zebec.configuration.Settings
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.Instant
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

    fun generateFeed(writePath: String = System.getProperty("user.dir")): Boolean {
        val blog = settings.blog ?: return false
        val entries = blog.getEntries(settings) ?: return false
        entries.forEach {
            println(blog.getEntryPath(it, settings))
        }

        val host = settings.host ?: return false

        // TODO: Add title and description to blog configuration
        val channel = Channel("eat. code. stonks.", URL("https://$host/blog"), "a blog")
        val mapper = ObjectMapper()
        val module = SimpleModule()
        module.addSerializer(GregorianCalendar::class.java, DateEncoder())
        mapper.registerModule(module)
        mapper.registerModule(KotlinModule())

        val serialized = mapper.writeValueAsString(channel)
        println(serialized)

        return true
    }

}