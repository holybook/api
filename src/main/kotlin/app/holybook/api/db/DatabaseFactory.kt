package app.holybook.api.db

import io.ktor.server.config.*
import app.holybook.api.models.Books
import app.holybook.api.models.Paragraphs
import app.holybook.api.models.Translations
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val driverClassName =
            config.property("storage.driverClassName").getString()
        val jdbcURL = config.property("storage.jdbcURL").getString()
        val database = Database.connect(jdbcURL, driverClassName)
        transaction(database) {
            SchemaUtils.create(Books)
            SchemaUtils.create(Translations)
            SchemaUtils.create(Paragraphs)
        }
    }

}