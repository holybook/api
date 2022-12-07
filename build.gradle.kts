import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion: String by project
val postgresqlVersion: String by project
val exposedVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.7.20"
    id("io.ktor.plugin") version "2.1.3"
    kotlin("plugin.serialization") version "1.7.20"
}

group = "app.holybook"
version = "1.0-SNAPSHOT"


repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-html-builder-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.1.3")
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("app.holybook.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}