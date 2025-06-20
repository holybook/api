import Deps.database
import Deps.xml

plugins {
    `java-library`
    id("common-dependencies")
    id("ktor-dependencies")
}

group = "app.holybook:lib"
version = "0.1.0"


repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.genai:google-genai:1+")
    database()
    xml()
}

tasks.test {
    useJUnit()
}