import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val postgresqlVersion: String by project
val hikariVersion: String by project
val logbackVersion: String by project

plugins {
    `java-library`
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
}

group = "app.holybook:lib"
version = "0.1.0"


repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation ("com.zaxxer:HikariCP:$hikariVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("javax.xml:jaxp-api:1.4.2")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}