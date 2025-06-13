import Deps.json
import Deps.pdfBox
import Deps.xml

plugins {
    application
    id("cli-application")
    id("ktor-dependencies")
}

group = "app.holybook:import"
version = "0.1.0-RC5"


repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":lib"))
    xml()
    json()
    pdfBox()
}

tasks.test {
    useJUnit()
}

application {
    mainClass.set("app.holybook.import.MainKt")
}