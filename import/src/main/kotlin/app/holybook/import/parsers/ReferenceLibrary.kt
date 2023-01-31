package app.holybook.import.parsers

import app.holybook.import.BookContent
import app.holybook.import.BookMetadata
import app.holybook.import.OriginalBook
import app.holybook.import.common.CONTENT_TYPES_XML
import app.holybook.import.common.ContentMatcher
import app.holybook.import.common.ContentParsingRule
import app.holybook.lib.models.ParagraphListBuilder
import app.holybook.lib.models.ParagraphType
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.textRecursively
import java.time.LocalDate
import java.time.format.DateTimeFormatter.BASIC_ISO_DATE

object ReferenceLibrary {

  val rule = ContentParsingRule(ContentMatcher(CONTENT_TYPES_XML, "bahai.org")) {
    it.parseWithKonsumer {
      parse()
    }
  }
  private fun Konsumer.parse(): OriginalBook {
    var title = ""
    var author = ""
    var date: LocalDate? = null
    val paragraphs = ParagraphListBuilder()
    child("html") {
      child("head") {
        allChildrenAutoIgnore(Names.of("meta", "title")) {
          when (localName) {
            "meta" ->
              when (attributes.getValueOrNull("name")) {
                "author" -> author = attributes.getValue("content")
                "date" -> date = LocalDate.parse(attributes.getValue("content"), BASIC_ISO_DATE)
              }
            "title" -> {
              title = text()
            }
          }
        }
      }
      child("body") {
        child("div") { recursiveGetParagraphs(paragraphs) }
        skipContents()
      }
    }
    return OriginalBook(BookMetadata(date), BookContent(title, author, paragraphs.build()))
  }

  private fun Konsumer.recursiveGetParagraphs(paragraphs: ParagraphListBuilder) {
    allChildrenAutoIgnore(Names.of("div", "p")) {
      when (localName) {
        "div" -> recursiveGetParagraphs(paragraphs)
        "p" -> {
          val classNames = attributes.getValueOrNull("class")
          paragraphs.addParagraph(textRecursively(), getParagraphType(classNames))
        }
      }
    }
  }
}

private fun getParagraphType(classNames: String?): ParagraphType {
  return when (classNames) {
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
