import gradle.kotlin.dsl.accessors._0fe83e200ed26695aaa149fee753aa39.implementation
import org.gradle.kotlin.dsl.DependencyHandlerScope

object Deps {
  fun DependencyHandlerScope.logging() {
    implementation("org.slf4j:slf4j-api:${Versions.slf4jVersion}")
    implementation("org.slf4j:slf4j-simple:${Versions.slf4jVersion}")
  }

  fun DependencyHandlerScope.xml() {
    implementation("org.jsoup:jsoup:1.+")
    implementation("com.gitlab.mvysny.konsume-xml:konsume-xml:1.+")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.+")
  }

  fun DependencyHandlerScope.json() {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.+")
  }

  fun DependencyHandlerScope.pdfBox() {
    implementation("org.apache.pdfbox:pdfbox:3.+")
  }

  fun DependencyHandlerScope.database() {
    implementation("org.postgresql:postgresql:42.+")
    implementation("org.xerial:sqlite-jdbc:3.+")
    implementation("com.zaxxer:HikariCP:6.+")
    implementation("com.google.cloud.sql:postgres-socket-factory:1.+")
  }

  fun DependencyHandlerScope.ktor() {
    implementation("io.ktor:ktor-client-jvm:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-client-cio-jvm:${Versions.ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktorVersion}")
  }

  fun DependencyHandlerScope.vertexAi() {
    implementation("com.google.genai:google-genai:1+")
    implementation("com.google.cloud:google-cloud-vertexai:1.+")
  }

  fun DependencyHandlerScope.dagger() {
    implementation("com.google.dagger:dagger:${Versions.daggerVersion}")
  }
}
