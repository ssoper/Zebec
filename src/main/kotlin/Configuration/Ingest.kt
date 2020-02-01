package com.seansoper.zebec.configuration

import java.nio.file.Path
import java.nio.file.Paths

data class BlogConfiguration(val directory: Path,
                             val extension: String)

data class Configuration(val source: Path,
                         val destination: Path,
                         val port: Int,
                         val extensions: Array<String>,
                         val templates: Map<String, Path>?,
                         val blog: BlogConfiguration?)

class Ingest(val basePath: String) {

    data class BlogIngestible(var directory: String? = null, var extension: String? = null)

    class Ingestible {
        var source: String? = null
        var destination: String? = null
        var port: Int? = null
        var extensions: Array<String>? = null
        var templates: Map<String, String>? = null
        var blogConfig: BlogIngestible? = null

        fun blog(block: BlogIngestible.() -> Unit) {
            val config = BlogIngestible()
            config.block()

            blogConfig = BlogIngestible(config.directory, config.extension)
        }
    }

    fun configure(block: Ingestible.() -> Unit): Configuration {
        val config = Ingestible()
        config.block()

        if (config.source == null) {
            throw InvalidConfigurationException("source")
        }

        val source = Paths.get(basePath, config.source!!)
        val destination = config.destination?.let {
            Paths.get(basePath, it)
        } ?: Paths.get(basePath, ".")
        val port = config.port ?: 8080
        val extensions = config.extensions ?: arrayOf("css", "js", "ktml", "md")
        val templates = config.templates?.mapValues { Paths.get(basePath, it.value) }
        val blog = config.blogConfig?.directory?.let { directory ->
            config.blogConfig?.extension?.let { extension ->
                BlogConfiguration(Paths.get(basePath, directory), extension)
            }
        }

        return Configuration(source, destination, port, extensions, templates, blog)
    }

}

class InvalidConfigurationException(field: String): Exception("Invalid configuration, missing $field")