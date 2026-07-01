package app.holybook.lib.models

import app.holybook.lib.db.Database.transaction
import app.holybook.lib.db.getLanguageConfiguration
import app.holybook.lib.db.map
import app.holybook.lib.serialization.ParagraphTypeSerializer
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import kotlinx.serialization.Serializable

fun Connection.createParagraphsTable() {
  createStatement()
    .executeUpdate(
      """
    CREATE TABLE IF NOT EXISTS paragraphs (
        book VARCHAR(128) NOT NULL,
        language VARCHAR(3) NOT NULL,
        search_configuration REGCONFIG NOT NULL,
        index INT NOT NULL,
        number INT NULL,
        section_path VARCHAR(64) NOT NULL DEFAULT '',
        type VARCHAR(64) NOT NULL,
        text TEXT NOT NULL,
        text_tokens tsvector
            GENERATED ALWAYS AS (to_tsvector(search_configuration, text)) STORED,
    
        PRIMARY KEY (book, language, index),
        FOREIGN KEY (book) REFERENCES books(id)
    );
    
    CREATE INDEX IF NOT EXISTS text_search_idx ON paragraphs USING GIN (text_tokens);
  """
        .trimIndent()
    )
}

fun Connection.dropParagraphsTable() {
  createStatement()
    .executeUpdate(
      """
    DROP TABLE IF EXISTS paragraphs;
  """
        .trimIndent()
    )
}

fun getParagraphs(bookId: String, language: String, startIndex: Int?, endIndex: Int?) =
  transaction {
    val startClause = if (startIndex != null) "AND index >= ?" else ""
    val endClause = if (endIndex != null) "AND index <= ?" else ""
    val getParagraphs =
      prepareStatement(
        """
      SELECT index, number, section_path, type, text FROM paragraphs
      WHERE book = ? AND language = ? $startClause $endClause
      ORDER BY index
    """
          .trimIndent()
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
  val getParagraphs =
    prepareStatement(
      """
      SELECT paragraphs.book, author, title, index, number, section_path, type, text, ts_headline(?::REGCONFIG, text, websearch_to_tsquery(?::REGCONFIG, ?)) as highlighted
      FROM paragraphs
      INNER JOIN books on paragraphs.book = books.id
      INNER JOIN translations on books.id = translations.book and translations.language = ?
      WHERE paragraphs.language = ? AND text_tokens @@ websearch_to_tsquery(?::REGCONFIG, ?)
    """
        .trimIndent()
    )
  getParagraphs.setString(1, languageConfiguration)
  getParagraphs.setString(2, languageConfiguration)
  getParagraphs.setString(3, query)
  getParagraphs.setString(4, language)
  getParagraphs.setString(5, language)
  getParagraphs.setString(6, languageConfiguration)
  getParagraphs.setString(7, query)
  getParagraphs.executeQuery().map {
    SearchResult(
      bookId = getString("book"),
      author = getAuthorName(getString("author"), language),
      title = getString("title"),
      highlightedText = getString("highlighted"),
      paragraph = currentParagraph(),
    )
  }
}

fun translate(request: TranslateRequest) = transaction {
  val languageConfiguration = getLanguageConfiguration(request.fromLanguage)
  val getParagraphs =
    prepareStatement(
      """
    SELECT paragraphs.book, author, title, index, number, section_path, type, text, ts_headline(?::REGCONFIG, text, websearch_to_tsquery(?::REGCONFIG, ?)) as highlighted
    FROM paragraphs
    INNER JOIN books on books.id = paragraphs.book
    INNER JOIN translations on books.id = translations.book and translations.language = ?
    WHERE paragraphs.language = ? AND text ILIKE ?
  """
        .trimIndent()
    )
  getParagraphs.setString(1, languageConfiguration)
  getParagraphs.setString(2, languageConfiguration)
  getParagraphs.setString(3, request.text)
  getParagraphs.setString(4, request.fromLanguage)
  getParagraphs.setString(5, request.fromLanguage)
  getParagraphs.setString(6, "%${request.text}%")
  val searchResults =
    getParagraphs.executeQuery().map {
      SearchResult(
        bookId = getString("book"),
        author = getAuthorName(getString("author"), request.fromLanguage),
        title = getString("title"),
        highlightedText = getString("highlighted"),
        paragraph = currentParagraph(),
      )
    }
  val firstResult = searchResults.firstOrNull() ?: return@transaction null
  val getTranslatedParagraph =
    prepareStatement(
      """
    SELECT index, number, section_path, type, text FROM paragraphs
    WHERE book = ? AND index = ? AND language = ?
  """
        .trimIndent()
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

private fun ResultSet.currentParagraph(): Paragraph {
  val number = getInt("number")
  return Paragraph(
    getInt("index"),
    getString("text"),
    ParagraphType.fromValue(getString("type"))!!,
    if (number > 0) number else null,
    getString("section_path") ?: "",
  )
}

fun Connection.insertParagraphs(bookId: String, language: String, paragraphs: List<Paragraph>) {
  val insertParagraph =
    prepareStatement(
      """
        INSERT INTO paragraphs(book, language, search_configuration, index, number, section_path, type, text)
        VALUES (?, ?, ?::REGCONFIG, ?, ?, ?, ?, ?)
    """
        .trimIndent()
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
    insertParagraph.setString(6, paragraph.sectionPath)
    insertParagraph.setString(7, paragraph.type.value)
    insertParagraph.setString(8, paragraph.text)
    insertParagraph.addBatch()
  }
  insertParagraph.executeBatch()
}

@Serializable
data class TranslateRequest(val fromLanguage: String, val toLanguage: String, val text: String)

@Serializable
data class TranslateResponse(
  val translatedParagraph: Paragraph,
  val allOriginalResults: List<SearchResult>,
)

@Serializable
data class Paragraph(
  val index: Int,
  val text: String,
  @Serializable(with = ParagraphTypeSerializer::class) val type: ParagraphType,
  val number: Int?,
  /**
   * Dotted path of the section this paragraph belongs to (e.g. `"1"`, `"1.1"`). Empty for
   * top-level paragraphs in flat books. Combined with [number] this yields the displayed label,
   * e.g. `1.1:5`.
   */
  val sectionPath: String = "",
)

@Serializable
data class SearchResult(
  val bookId: String,
  val author: String,
  val title: String,
  val highlightedText: String,
  val paragraph: Paragraph,
)

enum class ParagraphType(val value: String) {
  BODY("body"),
  SECTION_TITLE("section-title"),
  HEADER("header"),
  TITLE("title"),
  LETTER_HEAD("letter-head"),
  DATE("date"),
  ADDRESSEE("addressee"),
  SALUTATION("salutation"),
  SEPARATOR("separator"),
  SIGNATURE("signature");

  companion object {

    private val values = ParagraphType.values().associateBy(ParagraphType::value)

    fun fromValue(value: String) = values[value]
  }
}

/**
 * Builds a flat, indexed list of [Paragraph]s while tracking an arbitrarily deep section hierarchy.
 *
 * Sections are numbered per parent (`1`, `2`, then `1.1`, `1.2`, ...). Body paragraph numbers
 * restart at `1` within each section, so the rendered label is `sectionPath:number`
 * (e.g. `1.1:5`). Top-level paragraphs have an empty section path and a plain running number.
 */
class ParagraphListBuilder {
  private class Frame(val path: String) {
    var childCount = 0
    var bodyCounter = 0
  }

  private val paragraphs = mutableListOf<Paragraph>()
  private val stack = ArrayDeque(listOf(Frame("")))
  // Source heading level of each open section, parallel to [stack] (excluding the root frame). Used
  // by [heading] to translate a stream of leveled headings (e.g. <h2>/<h3>) into proper nesting.
  private val headingLevels = ArrayDeque<Int>()

  private val current
    get() = stack.last()

  /** Opens a new (sub)section, emitting a [ParagraphType.SECTION_TITLE] paragraph for its title. */
  fun enterSection(title: String) {
    val parent = current
    parent.childCount++
    val path =
      if (parent.path.isEmpty()) "${parent.childCount}" else "${parent.path}.${parent.childCount}"
    stack.addLast(Frame(path))
    paragraphs.add(Paragraph(paragraphs.size, title, ParagraphType.SECTION_TITLE, null, path))
  }

  /** Closes the current section, returning to its parent. */
  fun exitSection() {
    check(stack.size > 1) { "exitSection called without a matching enterSection" }
    stack.removeLast()
  }

  /** Runs [body] inside a freshly opened section, closing it afterwards. */
  inline fun section(title: String, body: ParagraphListBuilder.() -> Unit) {
    enterSection(title)
    body()
    exitSection()
  }

  /**
   * Opens a section for a heading at the given source [level] (e.g. `2` for `<h2>`), closing any
   * open sections at the same or deeper level first so that the resulting nesting mirrors the
   * heading hierarchy. Do not mix with manual [enterSection]/[exitSection] in the same build.
   */
  fun heading(level: Int, title: String) {
    while (headingLevels.isNotEmpty() && headingLevels.last() >= level) {
      exitSection()
      headingLevels.removeLast()
    }
    enterSection(title)
    headingLevels.addLast(level)
  }

  fun addParagraph(text: String, type: ParagraphType = ParagraphType.BODY) {
    val number = if (type == ParagraphType.BODY) ++current.bodyCounter else null
    paragraphs.add(Paragraph(paragraphs.size, text, type, number, current.path))
  }

  fun build(): List<Paragraph> = paragraphs
}

fun buildParagraphs(body: ParagraphListBuilder.() -> Unit): List<Paragraph> =
  ParagraphListBuilder().apply(body).build()

class ParagraphElement(val text: String, val type: ParagraphType = ParagraphType.BODY)

fun Iterable<ParagraphElement>.withIndices(): List<Paragraph> = buildParagraphs {
  this@withIndices.forEach { addParagraph(it.text, it.type) }
}
