package app.holybook.tools.importers

import app.holybook.tools.BookContent
import app.holybook.tools.Paragraph
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import java.lang.AssertionError

object BibliothekBahaiDe {

    val parser: ParagraphParser = XmlParser("bibliothek.bahai.de") {
        return@XmlParser parseGerman()
    }

    private fun Konsumer.parseGerman(): BookContent {
        var title = ""
        val paragraphs = mutableListOf<Paragraph>()
        child("doc") {
            child("metadata") {
                allChildrenAutoIgnore(Names.of("titleName")) {
                    when (localName) {
                        "titleName" -> title = text()
                    }
                }
            }
            child("main") {
                processDiv()
            }
        }
        return BookContent(title, paragraphs)
    }

    private fun Konsumer.processDiv() {
        children(Names.of("div", "par", "heading")) {
            when (localName) {
                "div" -> processDiv()
                "par" -> processPar("body")
                "heading" -> processPar("header")
                else -> throw AssertionError("error")
            }
        }
    }

    private fun Konsumer.processPar(type: String): Paragraph {
        allChildrenAutoIgnore(Names.of()) {}
        return Paragraph(text(), type)
    }

}