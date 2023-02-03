import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val postgresqlVersion: String by project
val hikariVersion: String by project
val exposedVersion: String by project
val h2Version: String by project
val logbackVersion: String by project

plugins {
  application
  kotlin("jvm") version "1.7.20"
  kotlin("plugin.serialization") version "1.7.20"
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
  implementation("io.ktor:ktor-client-jvm:$ktorVersion")
  implementation("org.slf4j:slf4j-api:1.7.32")
  implementation("org.slf4j:slf4j-simple:1.7.32")
  implementation("ch.qos.logback:logback-classic:$logbackVersion")
  implementation("commons-cli:commons-cli:1.5.0")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
  implementation("org.jsoup:jsoup:1.15.3")
}

tasks.test { useJUnit() }

tasks.withType<KotlinCompile>() { kotlinOptions.jvmTarget = "1.8" }

application { mainClass.set("app.holybook.import.MainKt") }
