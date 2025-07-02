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
}