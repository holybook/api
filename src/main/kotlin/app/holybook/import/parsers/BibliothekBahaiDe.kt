package app.holybook.import.parsers

import app.holybook.api.models.Paragraph
import app.holybook.import.BookContent
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.textRecursively
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
                "par" -> processPar(paragraphs)
                "heading" -> paragraphs.add(Paragraph(paragraphs.size, text(), "header"))
                else -> throw AssertionError("error")
            }
        }
    }

    private fun Konsumer.processPar(
        paragraphs: MutableList<Paragraph>
    ) {
        childOrNull("address") { skipContents() }
        paragraphs.add(Paragraph(paragraphs.size, text(), "body"))
    }

}