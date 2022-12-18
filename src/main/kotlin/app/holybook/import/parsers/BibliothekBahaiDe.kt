package app.holybook.import.parsers

import app.holybook.import.BookContent
import app.holybook.import.Paragraph
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import java.lang.AssertionError

object BibliothekBahaiDe {

    val parser: ParagraphParser = XmlParser("bibliothek.bahai.de") {
        return@XmlParser parse()
    }

    private fun Konsumer.parse(): BookContent {
        var title = ""
        var author = ""
        val paragraphs = mutableListOf<Paragraph>()
        child("doc") {
            child("metadata") {
                allChildrenAutoIgnore(Names.of("titleName")) {
                    when (localName) {
                        "titleName" -> title = text()
                        "authorName" -> author = text()
                    }
                }
            }
            child("main") {
                processDiv(paragraphs)
            }
        }
        return BookContent(title, author, paragraphs)
    }

    private fun Konsumer.processDiv(paragraphs: MutableList<Paragraph>) {
        children(Names.of("div", "par", "heading")) {
            when (localName) {
                "div" -> processDiv(paragraphs)
                "par" -> processPar("body", paragraphs)
                "heading" -> processPar("header", paragraphs)
                else -> throw AssertionError("error")
            }
        }
    }

    private fun Konsumer.processPar(
        type: String,
        paragraphs: MutableList<Paragraph>
    ) {
        allChildrenAutoIgnore(Names.of()) {}
        paragraphs.add(Paragraph(text(), type))
    }

}