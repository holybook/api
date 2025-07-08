import Deps.dagger

plugins {
  `java-library`
  id("kotlin-kapt")
}

repositories {
  mavenCentral()
  gradlePluginPortal()
}

dependencies {
  dagger()
  "kapt"("com.google.dagger:dagger-compiler:${Versions.daggerVersion}")
}
