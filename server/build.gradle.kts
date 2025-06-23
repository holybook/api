import Deps.database
import Deps.pdfBox
import Deps.xml

plugins {
    application
    id("common-dependencies")
    id("ktor-dependencies")
}

group = "app.holybook:server"
version = "0.1.1"


repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(project(":lib"))
    implementation("io.ktor:ktor-server-forwarded-header:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-status-pages:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-call-logging:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-netty-jvm:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-html-builder-jvm:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-content-negotiation:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-call-logging:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-cors:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-server-call-logging-jvm:${Versions.ktorVersion}")
    implementation("com.github.Benjozork:exposed-postgres-extensions:master-SNAPSHOT")
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    database()
    xml()
    pdfBox()
}

tasks.test {
    useJUnit()
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=true")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}