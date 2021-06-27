package com.seansoper.zebec.fileProcessor

import com.seansoper.zebec.KTMLParser
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class KTML(val verbose: Boolean): Processable {

    override fun process(content: String): String? {
        val engine = ScriptEngineManager().getEngineByExtension("kts")

        // Because bindings are wonky
        val updatedContent = "import com.seansoper.zebec.KTMLParser\n$content"
            .replace("html ", "KTMLParser().html ")
            .replace("LinkRelType", "KTMLParser.LinkRelType")

        return try {
            val compiled = engine.eval(updatedContent) as KTMLParser.HTML
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