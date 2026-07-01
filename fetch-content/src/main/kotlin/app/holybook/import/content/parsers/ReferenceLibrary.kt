package app.holybook.import.content.parsers

import app.holybook.import.common.CONTENT_TYPES_XML
import app.holybook.import.common.ContentMatcher
import app.holybook.lib.models.ParagraphType
import app.holybook.lib.models.buildParagraphs
import app.holybook.lib.parsers.ContentParsingRule
import app.holybook.lib.parsers.parse
import org.jsoup.nodes.Document

object ReferenceLibrary {

  val rule =
    ContentParsingRule(ContentMatcher(CONTENT_TYPES_XML, "bahai.org")) { it.parse { parse() } }

  private fun Document.parse(): ParsedBook {
    val body = selectFirst("div.b")
    // Real heading tags (<h2>..<h6>) carry the section nesting of structured works; <h1> is the
    // work title and is dropped (it is captured as metadata). A HEADER-class paragraph (used by
    // some letters) is treated as the deepest heading. Works without any of these stay flat.
    val paragraphs = buildParagraphs {
      body?.select("h1, h2, h3, h4, h5, h6, p")?.forEach { element ->
        // Skip table-of-contents entries, which the source renders inside <li>.
        if (element.parents().any { it.tagName() == "li" }) return@forEach
        val text = element.text().trim()
        if (text.isEmpty()) return@forEach
        val tag = element.tagName().lowercase()
        when {
          tag == "h1" -> {}
          tag.length == 2 && tag[0] == 'h' -> heading(tag[1].digitToInt(), text)
          else -> {
            val type = getParagraphType(element.className())
            if (type == ParagraphType.HEADER) heading(7, text) else addParagraph(text, type)
          }
        }
      }
    }
    return ParsedBook(title = selectFirst("head title")?.text() ?: "", paragraphs = paragraphs)
  }
}

private fun getParagraphType(classNames: String?): ParagraphType {
  return when (classNames) {
    "c xb" -> ParagraphType.LETTER_HEAD
    "c yb" -> ParagraphType.LETTER_HEAD
    "c zb" -> ParagraphType.LETTER_HEAD
    "c ab" -> ParagraphType.LETTER_HEAD
    "cc" -> ParagraphType.DATE
    "wb" -> ParagraphType.DATE
    "ub" -> ParagraphType.ADDRESSEE
    "tb" -> ParagraphType.SALUTATION
    "c cd hb" -> ParagraphType.HEADER
    "sb vb" -> ParagraphType.SEPARATOR
    "pb" -> ParagraphType.SIGNATURE
    else -> ParagraphType.BODY
  }
}
