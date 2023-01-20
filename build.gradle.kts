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
    id("io.ktor.plugin") version "2.2.2"
    kotlin("plugin.serialization") version "1.7.20"
}

group = "app.holybook"
version = "0.1.0-RC5"


repositories {
    jcenter()
    mavenCentral()
    maven("https://jitpack.io")
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
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.1.3")
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation ("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.apache.pdfbox:pdfbox:2.0.27")
    implementation("com.gitlab.mvysny.konsume-xml:konsume-xml:1.0")
    implementation("com.h2database:h2:$h2Version")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("com.github.Benjozork:exposed-postgres-extensions:master-SNAPSHOT")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}