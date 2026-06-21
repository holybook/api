import org.gradle.kotlin.dsl.DependencyHandlerScope

object Deps {
  private fun DependencyHandlerScope.impl(dep: String) = add("implementation", dep)

  fun DependencyHandlerScope.logging() {
    impl("org.slf4j:slf4j-api:${Versions.slf4jVersion}")
    impl("org.slf4j:slf4j-simple:${Versions.slf4jVersion}")
  }

  fun DependencyHandlerScope.xml() {
    impl("org.jsoup:jsoup:1.+")
    impl("com.gitlab.mvysny.konsume-xml:konsume-xml:1.+")
    impl("org.jetbrains.kotlinx:kotlinx-html-jvm:0.+")
  }

  fun DependencyHandlerScope.json() {
    impl("org.jetbrains.kotlinx:kotlinx-serialization-json:1.+")
  }

  fun DependencyHandlerScope.pdfBox() {
    impl("org.apache.pdfbox:pdfbox:3.+")
  }

  fun DependencyHandlerScope.database() {
    impl("org.postgresql:postgresql:42.+")
    impl("org.xerial:sqlite-jdbc:3.+")
    impl("com.zaxxer:HikariCP:6.+")
    impl("com.google.cloud.sql:postgres-socket-factory:1.+")
  }

  fun DependencyHandlerScope.ktor() {
    impl("io.ktor:ktor-client-jvm:${Versions.ktorVersion}")
    impl("io.ktor:ktor-client-cio-jvm:${Versions.ktorVersion}")
    impl("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktorVersion}")
  }

  fun DependencyHandlerScope.vertexAi() {
    impl("com.google.genai:google-genai:1+")
    impl("com.google.cloud:google-cloud-vertexai:1.+")
  }

  fun DependencyHandlerScope.dagger() {
    impl("com.google.dagger:dagger:${Versions.daggerVersion}")
  }
}
