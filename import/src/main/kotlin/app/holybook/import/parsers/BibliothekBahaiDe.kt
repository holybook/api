package app.holybook.import.parsers

import app.holybook.import.BookContent
import app.holybook.import.common.CONTENT_TYPES_XML
import app.holybook.import.common.ContentMatcher
import app.holybook.import.common.ContentParsingRule
import app.holybook.lib.models.ParagraphElement
import app.holybook.lib.models.ParagraphType
import app.holybook.lib.models.withIndices
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.jsoup.nodes.Document

object BibliothekBahaiDe {

  val rule =
    ContentParsingRule(ContentMatcher(CONTENT_TYPES_XML, "bibliothek.bahai.de")) {
      it.parse { parse() }
    }

  private fun Document.parse(): BookContent {
    val paragraphs =
      select("par")
        .map { ParagraphElement(it.text(), getParagraphType(it.className())) }
        .withIndices()
    return BookContent(
      title = select("metadata titleName").text(),
      author = select("metadata authorName").text(),
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
