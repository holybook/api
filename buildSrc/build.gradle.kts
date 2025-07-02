plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.+")
    implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:2.+")
    implementation("io.ktor.plugin:io.ktor.plugin.gradle.plugin:3.+")
    implementation("com.google.dagger:dagger-compiler:2.+")
}