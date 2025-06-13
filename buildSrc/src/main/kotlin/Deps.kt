import gradle.kotlin.dsl.accessors._0449600b324c89f28a01158b532ae80c.implementation
import org.gradle.kotlin.dsl.DependencyHandlerScope

object Deps {
    fun DependencyHandlerScope.logging() {
        implementation("org.slf4j:slf4j-api:${Versions.slf4jVersion}")
        implementation("org.slf4j:slf4j-simple:${Versions.slf4jVersion}")
//        implementation("ch.qos.logback:logback-classic:${Versions.logbackVersion}")
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
        implementation ("com.zaxxer:HikariCP:6.+")
    }
}