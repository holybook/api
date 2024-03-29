package app.holybook.import.content.parsers

import app.holybook.import.common.CONTENT_TYPES_XML
import app.holybook.import.common.ContentMatcher
import app.holybook.lib.models.ParagraphElement
import app.holybook.lib.models.ParagraphType
import app.holybook.lib.models.withIndices
import app.holybook.lib.parsers.ContentParsingRule
import app.holybook.lib.parsers.parse
import org.jsoup.nodes.Document

object BibliothekBahaiDe {

  val rule =
    ContentParsingRule(ContentMatcher(CONTENT_TYPES_XML, "bibliothek.bahai.de")) {
      it.parse { parse() }
    }

  private fun Document.parse(): ParsedBook {
    val paragraphs =
      select("par")
        .map { ParagraphElement(it.ownText(), getParagraphType(it.className())) }
        .withIndices()
    return ParsedBook(title = select("metadata titleName").text(), paragraphs = paragraphs)
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
