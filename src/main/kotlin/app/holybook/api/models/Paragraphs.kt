package app.holybook.api.models

import app.holybook.api.db.Database.transaction
import java.sql.Connection
import kotlinx.serialization.Serializable

fun getParagraphs(bookId: String, language: String, startIndex: Int?, endIndex: Int?) =
  transaction {
    val startClause = if (startIndex != null) "AND index >= ?" else ""
    val endClause = if (endIndex != null) "AND index <= ?" else ""
    val getParagraphs = prepareStatement("""
      SELECT index, type, text FROM paragraphs 
      WHERE book = ? AND language = ? $startClause $endClause 
      ORDER BY index 
    """.trimIndent())
    getParagraphs.setString(1, bookId)
    getParagraphs.setString(2, language)
    var colIndex = 3
    if (startIndex != null) {
      getParagraphs.setInt(colIndex++, startIndex)
    }
    if (endIndex != null) {
      getParagraphs.setInt(colIndex, endIndex)
    }
    val paragraphRows = getParagraphs.executeQuery()
    val paragraphs = mutableListOf<Paragraph>()
    while (paragraphRows.next()) {
      paragraphs.add(Paragraph(paragraphRows.getInt("index"),
                               paragraphRows.getString("text"),
                               paragraphRows.getString("type")))
    }
    paragraphs
  }

fun Connection.insertParagraphs(bookId: String, language: String, paragraphs: List<Paragraph>) {
  val insertParagraph = prepareStatement("""
        INSERT INTO paragraphs(book, language, index, type, text)
        VALUES (?, ?, ?, ?, ?)
    """.trimIndent())
  paragraphs.forEach { paragraph ->
    insertParagraph.setString(1, bookId)
    insertParagraph.setString(2, language)
    insertParagraph.setInt(3, paragraph.index)
    insertParagraph.setString(4, paragraph.type)
    insertParagraph.setString(5, paragraph.text)
    insertParagraph.addBatch()
  }
  insertParagraph.executeBatch()
}

@Serializable
data class Paragraph(val index: Int, val text: String, val type: String)