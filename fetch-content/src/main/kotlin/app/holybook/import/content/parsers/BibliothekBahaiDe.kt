package app.holybook.import.content.parsers

import app.holybook.import.common.CONTENT_TYPES_XML
import app.holybook.import.common.ContentMatcher
import app.holybook.lib.models.ParagraphElement
import app.holybook.lib.models.ParagraphType
import app.holybook.lib.models.getAuthorIdByName
import app.holybook.lib.models.withIndices
import app.holybook.lib.parsers.ContentParsingRule
import app.holybook.lib.parsers.parse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.jsoup.nodes.Document

object BibliothekBahaiDe {

  val rule =
    ContentParsingRule(ContentMatcher(CONTENT_TYPES_XML, "bibliothek.bahai.de")) {
      it.parse { parse() }
    }

  private fun Document.parse(): ParsedBook {
    val paragraphs =
      select("par")
        .map { ParagraphElement(it.text(), getParagraphType(it.className())) }
        .withIndices()
    return ParsedBook(
      title = select("metadata titleName").text(),
      author = getAuthorIdByName(select("metadata authorName").text()),
      publishedAt =
        LocalDate.parse(
          select("metadata translFromEditionCode").text(),
          DateTimeFormatter.ISO_DATE
        ),
      paragraphs = paragraphs
    )
  }

  private fun getParagraphType(classNames: String?): ParagraphType {
    return when (classNames) {
      "letter sender uhj" -> ParagraphType.LETTER_HEAD
      "letter date" -> ParagraphType.DATE
      "letter addressee" -> ParagraphType.ADDRESSEE
      "letter salutation" -> ParagraphType.SALUTATION
      "letter signed" -> ParagraphType.SIGNATURE
      else -> ParagraphType.BODY
    }
  }
}
