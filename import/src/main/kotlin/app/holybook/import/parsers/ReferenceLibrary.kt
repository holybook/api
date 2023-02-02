package app.holybook.import.parsers

import app.holybook.import.BookContent
import app.holybook.import.common.CONTENT_TYPES_XML
import app.holybook.import.common.ContentMatcher
import app.holybook.import.common.ContentParsingRule
import app.holybook.lib.models.ParagraphElement
import app.holybook.lib.models.ParagraphType
import app.holybook.lib.models.withIndices
import java.time.LocalDate
import java.time.format.DateTimeFormatter.BASIC_ISO_DATE
import org.jsoup.nodes.Document

object ReferenceLibrary {

  val rule =
    ContentParsingRule(ContentMatcher(CONTENT_TYPES_XML, "bahai.org")) {
      it.parse { parse() }
    }

  private fun Document.parse(): BookContent {
    val paragraphs =
      select("div.b p")
        .map { ParagraphElement(it.text(), getParagraphType(it.className())) }
        .withIndices()
    return BookContent(
      title = select("head title").text(),
      author = select("head meta[name=author]").attr("content"),
      publishedAt = LocalDate.parse(select("head meta[name=date]").attr("content"), BASIC_ISO_DATE),
      paragraphs = paragraphs
    )
  }

  // private fun Konsumer.parse(): BookContent {
  //   var title = ""
  //   var author = ""
  //   var date: LocalDate? = null
  //   val paragraphs = ParagraphListBuilder()
  //   child("html") {
  //     child("head") {
  //       allChildrenAutoIgnore(Names.of("meta", "title")) {
  //         when (localName) {
  //           "meta" ->
  //             when (attributes.getValueOrNull("name")) {
  //               "author" -> author = attributes.getValue("content")
  //               "date" -> date = LocalDate.parse(attributes.getValue("content"), BASIC_ISO_DATE)
  //             }
  //           "title" -> {
  //             title = text()
  //           }
  //         }
  //       }
  //     }
  //     child("body") {
  //       child("div") { recursiveGetParagraphs(paragraphs) }
  //       skipContents()
  //     }
  //   }
  //   return BookContent(title, author, date, paragraphs.build())
  // }
  //
  // private fun Konsumer.recursiveGetParagraphs(paragraphs: ParagraphListBuilder) {
  //   allChildrenAutoIgnore(Names.of("div", "p")) {
  //     when (localName) {
  //       "div" -> recursiveGetParagraphs(paragraphs)
  //       "p" -> {
  //         val classNames = attributes.getValueOrNull("class")
  //         paragraphs.addParagraph(textRecursively(), getParagraphType(classNames))
  //       }
  //     }
  //   }
  // }
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
