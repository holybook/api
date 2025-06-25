import Deps.database
import Deps.ktor
import Deps.xml

plugins {
    `java-library`
    id("common-dependencies")
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
    ktor()
    database()
    xml()
}

tasks.test {
    useJUnit()
}
