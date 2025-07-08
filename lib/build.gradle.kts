import Deps.database
import Deps.ktor
import Deps.vertexAi
import Deps.xml

plugins {
  `java-library`
  id("common-dependencies")
  id("dagger")
}

group = "app.holybook:lib"

version = "0.1.0"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  testImplementation(kotlin("test"))
  ktor()
  database()
  xml()
  vertexAi()
}

tasks.test { useJUnit() }
