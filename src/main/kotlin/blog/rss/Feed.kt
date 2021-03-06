package com.seansoper.zebec.blog.rss

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "rss")
data class Feed(
    val channel: Channel,

    @field:JacksonXmlProperty(isAttribute = true)
    val version: String = "2.0",

    @field:JacksonXmlProperty(isAttribute = true, localName = "xmlns:atom")
    val xmlns: String = "http://www.w3.org/2005/Atom"
)