import Deps.database
import Deps.ktor
import Deps.xml

plugins {
    `java-library`
    id("common-dependencies")
    id("gg.jte.gradle") version "3.+"
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
    implementation("gg.jte:jte:3.+")
    implementation("gg.jte:jte-kotlin:3.+")
    implementation("gg.jte:jte-runtime:3.+")
    jteGenerate("gg.jte:jte-models:3.+")
    ktor()
    database()
    xml()
}

tasks.test {
    useJUnit()
}

jte {
    generate()

    jteExtension("gg.jte.models.generator.ModelExtension") {
        // Target language ("Java" and "Kotlin" are supported). "Java" is the default.
        property("language", "Kotlin")
    }
}
