package app.holybook.api.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.*

object Translations : Table() {
    val bookId = reference("book", Books)
    val language = varchar("language", 3)
    val title = varchar("title", 512)
    val lastModified = datetime("last_modified").nullable()

    override val primaryKey = PrimaryKey(bookId, language)
}