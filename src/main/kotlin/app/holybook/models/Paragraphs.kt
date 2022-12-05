package app.holybook.models

import org.jetbrains.exposed.sql.Table

object Paragraphs : Table() {
    val bookId = reference("book", Books)
    val index = integer("index")
    val language = varchar("language", 3)
    val text = text("text")

    override val primaryKey = PrimaryKey(bookId, index, language)
}