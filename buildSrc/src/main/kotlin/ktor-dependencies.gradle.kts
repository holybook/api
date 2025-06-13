plugins {
    `java-library`
    id("io.ktor.plugin")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("io.ktor:ktor-client-jvm:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio-jvm:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktorVersion}")
}