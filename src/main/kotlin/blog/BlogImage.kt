package com.seansoper.zebec.blog

import com.seansoper.zebec.relativeProtocol
import java.net.URL

class BlogImage(val imageURL: URL) {
    val unsplashImage: UnsplashImage?

    enum class Type {
        Entry,
        Preview
    }

    val socialMediaTag: String
        get() {
            return unsplashImage?.let {
                "<meta property='og:image' content='${it.relativeUrl}/1200x627' />"
            } ?: "<meta property='og:image' content='$imageURL' />"
        }

    init {
        unsplashImage = try {
            UnsplashImage(imageURL)
        } catch (_: InvalidUnsplashURL) {
            null
        }
    }

    fun imageHtmlAttributes(type: Type): String {
        return if (type == Type.Entry) {
            unsplashImage?.let {
                "src='${it.entryUrlNormal}' srcset='${it.entryUrlNormal} 1x, ${it.entryUrlRetina} 2x'"
            } ?: "src='${imageURL.relativeProtocol}'"
        } else {
            unsplashImage?.let {
                "src='${it.previewUrlNormal}' srcset='${it.previewUrlNormal} 1x, ${it.previewUrlRetina} 2x'"
            } ?: "src='${imageURL.relativeProtocol}'"
        }
    }

}