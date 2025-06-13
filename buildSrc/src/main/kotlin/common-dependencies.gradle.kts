import Deps.logging

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    logging()
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.+")
}