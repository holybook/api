package app.holybook.api.models

import app.holybook.api.db.map
import app.holybook.api.db.Database.transaction
import app.holybook.api.db.languageConfigurationMapping
import java.sql.Connection
import java.sql.ResultSet
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
    getParagraphs.executeQuery().map { currentParagraph() }
  }

fun searchParagraphs(language: String, query: String) = transaction {
  val getParagraphs = prepareStatement("""
      SELECT book, index, type, text FROM paragraphs 
      WHERE language = ? AND text_tokens @@ websearch_to_tsquery(?)
    """.trimIndent())
  getParagraphs.setString(1, language)
  getParagraphs.setString(2, query)
  getParagraphs.executeQuery().map {
    SearchResult(
      bookId = getString("book"),
      paragraph = currentParagraph()
    )
  }
}

private fun ResultSet.currentParagraph() =
  Paragraph(
    getInt("index"),
    getString("text"),
    getString("type")
  )

fun Connection.insertParagraphs(bookId: String, language: String, paragraphs: List<Paragraph>) {
  val insertParagraph = prepareStatement("""
        INSERT INTO paragraphs(book, language, search_configuration, index, type, text)
        VALUES (?, ?, ?::REGCONFIG, ?, ?, ?)
    """.trimIndent())
  paragraphs.forEach { paragraph ->
    insertParagraph.setString(1, bookId)
    insertParagraph.setString(2, language)
    insertParagraph.setString(3, languageConfigurationMapping[language] ?: "simple")
    insertParagraph.setInt(4, paragraph.index)
    insertParagraph.setString(5, paragraph.type)
    insertParagraph.setString(6, paragraph.text)
    insertParagraph.addBatch()
  }
  insertParagraph.executeBatch()
}

@Serializable
data class Paragraph(val index: Int, val text: String, val type: String)

@Serializable
data class SearchResult(val bookId: String, val paragraph: Paragraph)