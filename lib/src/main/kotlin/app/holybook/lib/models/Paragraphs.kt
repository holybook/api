package app.holybook.lib.models

import app.holybook.lib.db.map
import app.holybook.lib.db.Database.transaction
import app.holybook.lib.db.getLanguageConfiguration
import java.sql.Connection
import java.sql.ResultSet
import kotlinx.serialization.Serializable
import java.sql.Types

fun Connection.createParagraphsTable() {
    createStatement().executeUpdate(
        """
    CREATE TABLE IF NOT EXISTS paragraphs (
        book VARCHAR(32) NOT NULL,
        language VARCHAR(3) NOT NULL,
        search_configuration REGCONFIG NOT NULL,
        index INT NOT NULL,
        number INT NULL,
        type VARCHAR(64) NOT NULL,
        text TEXT NOT NULL,
        text_tokens tsvector
            GENERATED ALWAYS AS (to_tsvector(search_configuration, text)) STORED,
    
        PRIMARY KEY (book, language, index),
        FOREIGN KEY (book) REFERENCES books(id)
    );
    
    CREATE INDEX IF NOT EXISTS text_search_idx ON paragraphs USING GIN (text_tokens);
  """.trimIndent()
    )
}

fun Connection.dropParagraphsTable() {
    createStatement().executeUpdate(
        """
    DROP TABLE IF EXISTS paragraphs;
  """.trimIndent()
    )
}

fun getParagraphs(
    bookId: String,
    language: String,
    startIndex: Int?,
    endIndex: Int?,
) =
    transaction {
        val startClause = if (startIndex != null) "AND index >= ?" else ""
        val endClause = if (endIndex != null) "AND index <= ?" else ""
        val getParagraphs = prepareStatement(
            """
      SELECT index, number, type, text FROM paragraphs 
      WHERE book = ? AND language = ? $startClause $endClause 
      ORDER BY index 
    """.trimIndent()
        )
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
    val languageConfiguration = getLanguageConfiguration(language)
    val getParagraphs = prepareStatement(
        """
      SELECT book, index, number, type, text, ts_headline(?::REGCONFIG, text, websearch_to_tsquery(?::REGCONFIG, ?)) as highlighted 
      FROM paragraphs 
      WHERE language = ? AND text_tokens @@ websearch_to_tsquery(?::REGCONFIG, ?)
    """.trimIndent()
    )
    getParagraphs.setString(1, languageConfiguration)
    getParagraphs.setString(2, languageConfiguration)
    getParagraphs.setString(3, query)
    getParagraphs.setString(4, language)
    getParagraphs.setString(5, languageConfiguration)
    getParagraphs.setString(6, query)
    getParagraphs.executeQuery().map {
        SearchResult(
            bookId = getString("book"),
            highlightedText = getString("highlighted"),
            paragraph = currentParagraph()
        )
    }
}

fun translate(request: TranslateRequest) = transaction {
    val languageConfiguration = getLanguageConfiguration(request.fromLanguage)
    val getParagraphs = prepareStatement(
        """
    SELECT book, index, number, type, text, ts_headline(?::REGCONFIG, text, websearch_to_tsquery(?::REGCONFIG, ?)) as highlighted 
    FROM paragraphs 
    WHERE language = ? AND text ILIKE ?
  """.trimIndent()
    )
    getParagraphs.setString(1, languageConfiguration)
    getParagraphs.setString(2, languageConfiguration)
    getParagraphs.setString(3, request.text)
    getParagraphs.setString(4, request.fromLanguage)
    getParagraphs.setString(5, "%${request.text}%")
    val searchResults = getParagraphs.executeQuery().map {
        SearchResult(
            bookId = getString("book"),
            highlightedText = getString("highlighted"),
            paragraph = currentParagraph()
        )
    }
    val firstResult = searchResults.firstOrNull() ?: return@transaction null
    val getTranslatedParagraph = prepareStatement(
        """
    SELECT index, number, type, text FROM paragraphs
    WHERE book = ? AND index = ? AND language = ?
  """.trimIndent()
    )
    getTranslatedParagraph.setString(1, firstResult.bookId)
    getTranslatedParagraph.setInt(2, firstResult.paragraph.index)
    getTranslatedParagraph.setString(3, request.toLanguage)
    val results = getTranslatedParagraph.executeQuery()
    if (!results.next()) {
        return@transaction null
    }

    val translatedParagraph = results.currentParagraph()
    TranslateResponse(translatedParagraph, searchResults)
}

private fun ResultSet.currentParagraph() =
    Paragraph(
        getInt("index"),
        getString("text"),
        ParagraphType.fromValue(getString("type"))!!,
        getInt("number")
    )

fun Connection.insertParagraphs(
    bookId: String,
    language: String,
    paragraphs: List<Paragraph>,
) {
    val insertParagraph = prepareStatement(
        """
        INSERT INTO paragraphs(book, language, search_configuration, index, number, type, text)
        VALUES (?, ?, ?::REGCONFIG, ?, ?, ?, ?)
    """.trimIndent()
    )
    paragraphs.forEach { paragraph ->
        insertParagraph.setString(1, bookId)
        insertParagraph.setString(2, language)
        insertParagraph.setString(3, getLanguageConfiguration(language))
        insertParagraph.setInt(4, paragraph.index)
        if (paragraph.number != null) {
            insertParagraph.setInt(5, paragraph.number)
        } else {
            insertParagraph.setNull(5, Types.INTEGER)
        }
        insertParagraph.setString(6, paragraph.type.value)
        insertParagraph.setString(7, paragraph.text)
        insertParagraph.addBatch()
        insertParagraph.executeBatch()
    }
}

@Serializable
data class TranslateRequest(
    val fromLanguage: String,
    val toLanguage: String,
    val text: String,
)

@Serializable
data class TranslateResponse(
    val translatedParagraph: Paragraph,
    val allOriginalResults: List<SearchResult>,
)

@Serializable
data class Paragraph(
    val index: Int,
    val text: String,
    val type: ParagraphType,
    val number: Int?
)

@Serializable
data class SearchResult(
    val bookId: String,
    val highlightedText: String,
    val paragraph: Paragraph,
)

enum class ParagraphType(val value: String) {
    BODY("body"),
    HEADER("header"),
    TITLE("title"),
    LETTER_HEAD("letterhead"),
    DATE("date"),
    ADDRESSEE("addressee"),
    SALUTATION("salutation"),
    SEPARATOR("separator"),
    SIGNATURE("signature");

    companion object {

        private val values =
            ParagraphType.values().associateBy(ParagraphType::value)

        fun fromValue(value: String) = values[value]
    }
}

class ParagraphListBuilder {
    private val paragraphs = mutableListOf<Paragraph>()
    private var nextNumber = 1

    fun addParagraph(text: String, type: ParagraphType = ParagraphType.BODY) {
        paragraphs.add(Paragraph(paragraphs.size, text, type, getNumber(type)))
    }

    fun build(): List<Paragraph> = paragraphs

    private fun getNumber(type: ParagraphType) =
        if (type == ParagraphType.BODY) {
            nextNumber++
        } else {
            null
        }

}