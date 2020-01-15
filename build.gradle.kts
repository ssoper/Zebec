plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.61"
}

group = "com.seansoper.Zebec"
version = "1.0"

repositories {
    mavenCentral()
}

var jsr223WorkingVersion = "1.3.21"

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