package com.seansoper.zebec

import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.LocalDateTime
import java.time.ZoneOffset

object Utilities {

    // Credit: https://stackoverflow.com/a/59234917
    fun humanReadableByteCount(intBytes: Int): String {
        val bytes = intBytes.toLong()

        return when {
            bytes == Long.MIN_VALUE || bytes < 0 -> "N/A"
            bytes < 1024L -> "$bytes bytes"
            bytes <= 0xfffccccccccccccL shr 40 -> "%.1f KB".format(bytes.toDouble() / (0x1 shl 10))
            bytes <= 0xfffccccccccccccL shr 30 -> "%.1f MB".format(bytes.toDouble() / (0x1 shl 20))
            bytes <= 0xfffccccccccccccL shr 20 -> "%.1f GB".format(bytes.toDouble() / (0x1 shl 30))
            bytes <= 0xfffccccccccccccL shr 10 -> "%.1f TB".format(bytes.toDouble() / (0x1 shl 40))
            bytes <= 0xfffccccccccccccL -> "%.1f PiB".format((bytes shr 10).toDouble() / (0x1 shl 40))
            else -> "%.1f EB".format((bytes shr 20).toDouble() / (0x1 shl 40))
        }
    }

}

fun Path.filenameNoExtension(): String? = this.fileName.toString().split(".").firstOrNull()

val Path.createdDate: LocalDateTime?
    get() {
        return try {
            (Files.getAttribute(this, "creationTime") as FileTime).
                toInstant().
                atOffset(ZoneOffset.UTC).
                toLocalDateTime()
        } catch (exception: IOException) {
            null
        }
    }

val URL.relativeProtocol: String
    get() = "//${this.host}${this.file}"
