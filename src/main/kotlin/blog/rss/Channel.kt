package com.seansoper.zebec.blog.rss

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import java.net.URL
import java.util.*

@JacksonXmlRootElement(localName = "channel")
data class Channel(
    val title: String,
    val link: URL,
    val description: String,
    val language: String = "en-US",
    val lastBuildDate: GregorianCalendar = GregorianCalendar(),
    val generator: String = "zebec",
    val docs: String = "http://blogs.law.harvard.edu/tech/rss",

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "item")
    val items: List<Entry> = emptyList()
)