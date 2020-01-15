import java.io.FileInputStream
import java.util.*

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

val jsr223WorkingVersion = "1.3.21"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$jsr223WorkingVersion")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$jsr223WorkingVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-util:$jsr223WorkingVersion")
    compile(files("src/main/libs/yuicompressor-2.4.8.jar"))

}

tasks.compileKotlin {
    this.kotlinOptions.jvmTarget = "1.8"
}

tasks.compileTestKotlin {
    this.kotlinOptions.jvmTarget = "1.8"
}

val githubProperties = Properties()
githubProperties.load(FileInputStream(rootProject.file("github.properties")))

publishing {
    publications {
        create<MavenPublication>("gpr") {
            run {
                groupId = "com.seansoper"
                artifactId = "Zebec"
                version = "1.0"
                artifact("$rootDir/out/artifacts/${artifactId}_main_jar/$artifactId.jar")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/ssoper/Zebec") // Github Package
            credentials {
                //Fetch these details from the properties file or from Environment variables
                username = githubProperties.get("gpr.usr") as String? ?: System.getenv("GPR_USER")
                password = githubProperties.get("gpr.key") as String? ?: System.getenv("GPR_API_KEY")
            }
        }
    }
}
