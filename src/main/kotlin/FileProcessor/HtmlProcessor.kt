package com.seansoper.zebec.FileProcessor

import com.seansoper.zebec.HtmlEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class HtmlProcessor(val verbose: Boolean): Processable {
    override fun process(content: String): String? {
        val engine = ScriptEngineManager().getEngineByExtension("kts")

        // Because bindings are wonky
        val updatedContent = "import com.seansoper.zebec.HtmlEngine\n$content".replace("html ", "HtmlEngine().html ")
            .replace("LinkRelType", "HtmlEngine.LinkRelType")

        return try {
            val compiled = engine.eval(updatedContent) as HtmlEngine.HTML
            compiled.render()
        } catch (exception: ScriptException) {
            if (verbose) {
                println("ERROR: Code didn’t compile")
                println(exception.localizedMessage)
            }

            null
        }
    }
}