package com.seansoper.zebec.configuration

import com.seansoper.zebec.CommandLineParser
import java.io.File
import java.nio.file.Path
import javax.script.ScriptEngineManager

class Settings(parsed: CommandLineParser.Parsed, basePath: String) {

    val source: Path
    val destination: Path
    val port: Int
    val extensions: Array<String>
    val templates: Map<String, Path>?
    val blog: BlogConfiguration?
    val verbose: Boolean

    private val configuration: Configuration

    init {
        val engine = ScriptEngineManager().getEngineByExtension("kts")
        val newValue = "Ingest(\"$basePath\").configure "
        val content = File(parsed.pathToConfigFile.toString()).readText().replace("configure ", newValue)
        val result = "import com.seansoper.zebec.configuration.Ingest\n$content"

        configuration = engine.eval(result) as Configuration

        source = configuration.source
        destination = configuration.destination
        port = configuration.port
        extensions = configuration.extensions
        templates = configuration.templates
        blog = configuration.blog
        verbose = parsed.verbose
    }
}