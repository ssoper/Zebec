package com.seansoper.zebec.blog

import java.lang.Exception
import java.net.URL

class UnsplashImage(imageURL: URL) {

    val photoId: String

    val relativeUrl: String
        get() = "//source.$Host/$photoId"

    val entryUrlNormal: String
        get() = "$relativeUrl/${EntryWidth}x${EntryHeight}"

    val entryUrlRetina: String
        get() = "$relativeUrl/${EntryWidth*2}x${EntryHeight*2}"

    val previewUrlNormal: String
        get() = "$relativeUrl/${PreviewWidth}x${PreviewHeight}"

    val previewUrlRetina: String
        get() = "$relativeUrl/${PreviewWidth*2}x${PreviewHeight*2}"

    private val Host = "unsplash.com"
    private val EntryWidth = 900
    private val EntryHeight = 300
    private val PreviewWidth = 700
    private val PreviewHeight = 233

    init {
        if (!imageURL.host.contains(Host)) {
            throw InvalidUnsplashURL()
        }

        var path = imageURL.path.toString()

        if (path.endsWith("/")) {
            path = path.substringBeforeLast("/")
        }

        photoId = path.split("/").lastOrNull()?.let { Regex("[0-9a-zA-Z]+").matchEntire(it)?.value }
            ?: throw InvalidUnsplashURL()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is UnsplashImage) {
            photoId == other.photoId
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return Integer.valueOf(photoId).hashCode()
    }
}

class InvalidUnsplashURL: Exception("Invalid Unsplash URL")