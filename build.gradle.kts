import java.io.FileInputStream
import java.lang.Integer.min
import java.util.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
    id("maven-publish")
    id("jacoco")
}

repositories {
    mavenCentral()
}

val jsr223WorkingVersion = "1.3.21"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation( "org.jetbrains.kotlin:kotlin-script-runtime:$jsr223WorkingVersion")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$jsr223WorkingVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-util:$jsr223WorkingVersion")
    compile(files("src/main/libs/yuicompressor-2.4.8.jar"))
    compile(files("src/main/libs/markdown-0.1.41.jar"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
}

tasks.compileKotlin {
    this.kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    this.kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    executionData("$buildDir/jacoco/test.exec")
    reports {
        xml.isEnabled = true
        xml.destination = File("$buildDir/reports/jacoco/report.xml")
    }
}

tasks.register("parseJacocoReport") {
    val inputFile = File("$buildDir/reports/jacoco/report.xml")
    data class CoverageResult(val type: String, val missed: Int, val covered: Int, val ratio: Double, val ratioStr: String)

    val parse = fun(type: String): CoverageResult? {
        val regex = Regex("<counter type=\"$type\" missed=\"(\\d+)\" covered=\"(\\d+)\"/>")
        return regex.findAll(inputFile.readText())?.lastOrNull()?.let {
            return if (it.groups.count() < 3) {
                println("WARN: No coverage data found for $type")
                null
            } else {
                val missed = it.groups[1]?.value?.toInt() ?: 0
                val covered = min(it.groups[2]?.value?.toInt() ?: 0, 100)
                val ratio = covered.toDouble()/missed.toDouble()
                val ratioStr =  "%.0f".format(ratio*100)
                CoverageResult(type.toLowerCase(), missed, covered, ratio, ratioStr)
            }
        }
    }

    val types = setOf("INSTRUCTION", "BRANCH", "LINE", "COMPLEXITY", "METHOD", "CLASS")
    val results = types.mapNotNull(parse)
    var output = results.joinToString {
        "\"${it.type}\": {\"missed\": ${it.missed}, \"covered\": ${it.covered}, \"ratio\": ${it.ratio}, \"ratioStr\": \"${it.ratioStr}%\"}"
    }

    val average = results.map { it.ratio }.average()
    val color = when(average) {
        in 0.0..0.25 -> "orange"
        in 0.26..0.5 -> "yellow"
        in 0.51..0.75 -> "yellowgreen"
        else -> "green"
    }

    val total = "%.0f".format(average*100)
    output += """
        , "total": "${total}%", "color": "$color"
    """.trimIndent()
    output = "{${output}}"

    val outputFile = File("$buildDir/reports/jacoco/report.json")
    outputFile.writeText(output)
}

tasks.register("createGistPayload") {
    val inputFile = File("$buildDir/reports/jacoco/report.json")
    var content = inputFile.readText().replace("\"", "\\\"")
    content = "{\"files\":{\"report.json\":{\"content\": \"$content\"}}}"

    val outputFile = File("$buildDir/gist.json")
    outputFile.writeText(content)
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            run {
                groupId = "com.seansoper"
                artifactId = "zebec"
                version = "1.0.1"
                artifact("$rootDir/out/artifacts/${artifactId}_main_jar/$artifactId.jar")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ssoper/Zebec") // Github Package
            credentials {
                val githubProperties = Properties()

                try {
                    githubProperties.load(FileInputStream(rootProject.file("github.properties")))
                } catch (exception: Exception) {
                    println("WARN: Couldn’t find github.properties file")
                }

                //Fetch these details from the properties file or from Environment variables
                username = githubProperties.get("gpr.usr") as String? ?: System.getenv("GPR_USER")
                password = githubProperties.get("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
            }
        }
    }
}
