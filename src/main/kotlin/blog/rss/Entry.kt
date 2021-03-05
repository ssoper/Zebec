package com.seansoper.zebec.blog.rss

import java.net.URL
import java.util.*

data class Entry(
    val title: String,
    val link: URL,
    val description: String,
    val pubDate: GregorianCalendar,
    val guid: String = link.toString()
)