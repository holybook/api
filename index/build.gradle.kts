import Deps.xml

plugins {
  application
  id("cli-application")
}

group = "app.holybook:index"

version = "0.1.0"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  testImplementation(kotlin("test"))
  implementation(project(":lib"))
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.+")
  xml()
}

tasks.test { useJUnit() }

application { mainClass.set("app.holybook.index.MainKt") }
