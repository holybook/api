plugins { id("common-dependencies") }

repositories { mavenCentral() }

dependencies {
  implementation("commons-cli:commons-cli:1.+")
  implementation("com.typesafe:config:1.+")
}
