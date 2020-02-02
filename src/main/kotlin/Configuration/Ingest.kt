package com.seansoper.zebec.configuration

import java.nio.file.Path
import java.nio.file.Paths

data class BlogConfiguration(val directory: Path,
                             val extension: String,
                             val template: Path)

data class Configuration(val source: Path,
                         val destination: Path,
                         val port: Int,
                         val extensions: Array<String>,
                         val blog: BlogConfiguration?)

class Ingest(val basePath: String) {

    data class BlogIngestible(var directory: String? = null,
                              var extension: String? = null,
                              var template: String? = null)

    class Ingestible {
        var source: String? = null
        var destination: String? = null
        var port: Int? = null
        var extensions: Array<String>? = null
        var blogConfig: BlogIngestible? = null

        fun blog(block: BlogIngestible.() -> Unit) {
            val config = BlogIngestible()
            config.block()

            blogConfig = BlogIngestible(config.directory, config.extension, config.template)
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
        val blog = config.blogConfig?.directory?.let { directory ->
            config.blogConfig?.extension?.let { extension ->
                config.blogConfig?.template?.let {
                    val template = Paths.get(basePath, it)
                    BlogConfiguration(Paths.get(basePath, directory), extension, template)
                }
            }
        }

        return Configuration(source, destination, port, extensions, blog)
    }

}

class InvalidConfigurationException(field: String): Exception("Invalid configuration, missing $field")