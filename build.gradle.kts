import java.io.FileInputStream
import java.lang.Integer.min
import java.util.*

val javaVersion = JavaVersion.VERSION_17
val jsr223WorkingVersion = "1.3.21"

plugins {
    kotlin("jvm") version "1.8.21"
    id("jacoco")
}

allprojects {
    jacoco {
        toolVersion = "0.8.7"
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
    implementation("org.jetbrains.kotlin:kotlin-script-runtime:$jsr223WorkingVersion")
    implementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:$jsr223WorkingVersion")
    implementation("org.jetbrains.kotlin:kotlin-script-util:$jsr223WorkingVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.11.2")
    implementation(files("src/main/libs/yuicompressor-2.4.8.jar"))
    implementation(files("src/main/libs/markdown-0.1.41.jar"))
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.0")
}

java {
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
}

tasks.compileKotlin {
    this.kotlinOptions.jvmTarget = javaVersion.toString()
}

tasks.compileTestKotlin {
    this.kotlinOptions.jvmTarget = javaVersion.toString()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    description = "Create an executable JAR with a command-line client"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)

    manifest.attributes.set("Main-Class", "com.seansoper.zebec.Core")

    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

tasks.jacocoTestReport {
    executionData("$buildDir/jacoco/test.exec")
    reports {
        xml.isEnabled = true
        html.isEnabled = true
        xml.destination = File("$buildDir/reports/jacoco/report.xml")
    }
}