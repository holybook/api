import Deps.json
import Deps.ktor
import Deps.pdfBox
import Deps.xml

plugins {
  application
  id("cli-application")
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
  ktor()
  xml()
  json()
  pdfBox()
}

tasks.test { useJUnit() }

application { mainClass.set("app.holybook.import.MainKt") }
