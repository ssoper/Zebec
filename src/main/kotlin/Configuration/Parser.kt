package com.seansoper.zebec.configuration

import java.io.File
import javax.script.ScriptEngineManager

class Parser(val pathToFile: String, basePath: String) {

    val configuration: Configuration

    init {
        val engine = ScriptEngineManager().getEngineByExtension("kts")
        val newValue = "Ingest(\"$basePath\").configure "
        val content = File(pathToFile).readText().replace("configure ", newValue)
        val result = "import com.seansoper.zebec.configuration.Ingest\n$content"

        configuration = engine.eval(result) as Configuration
    }
}