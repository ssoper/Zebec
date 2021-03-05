package com.seansoper.zebec.blog.rss

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement

@JacksonXmlRootElement(localName = "rss")
data class Feed(
    val channel: Channel
)