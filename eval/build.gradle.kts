import Deps.ktor
import Deps.xml

plugins {
  application
  id("cli-application")
  id("dagger")
}

group = "app.holybook:eval"

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
}

tasks.test { useJUnit() }

application { mainClass.set("app.holybook.eval.MainKt") }
