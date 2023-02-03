package app.holybook.import.content.parsers

import app.holybook.import.common.CONTENT_TYPES_XML
import app.holybook.import.common.ContentMatcher
import app.holybook.import.common.ContentParsingRule
import app.holybook.lib.models.BookContent
import app.holybook.lib.models.ParagraphElement
import app.holybook.lib.models.ParagraphType
import app.holybook.lib.models.getAuthorIdByName
import app.holybook.lib.models.withIndices
import app.holybook.lib.parsers.parse
import java.time.LocalDate
import java.time.format.DateTimeFormatter.BASIC_ISO_DATE
import org.jsoup.nodes.Document

object ReferenceLibrary {

  val rule =
    ContentParsingRule(ContentMatcher(CONTENT_TYPES_XML, "bahai.org")) { it.parse { parse() } }

  private fun Document.parse(): BookContent {
    val paragraphs =
      select("div.b p")
        .map { ParagraphElement(it.text(), getParagraphType(it.className())) }
        .withIndices()
    return BookContent(
      title = select("head title").text(),
      author = getAuthorIdByName(select("head meta[name=author]").attr("content")),
      publishedAt = LocalDate.parse(select("head meta[name=date]").attr("content"), BASIC_ISO_DATE),
      paragraphs = paragraphs
    )
  }
}

private fun getParagraphType(classNames: String?): ParagraphType {
  return when (classNames) {
    "c yb" -> ParagraphType.LETTER_HEAD
    "c zb" -> ParagraphType.LETTER_HEAD
    "wb" -> ParagraphType.DATE
    "ub" -> ParagraphType.ADDRESSEE
    "tb" -> ParagraphType.SALUTATION
    "c cd hb" -> ParagraphType.HEADER
    "sb vb" -> ParagraphType.SEPARATOR
    "pb" -> ParagraphType.SIGNATURE
    else -> ParagraphType.BODY
  }
}
