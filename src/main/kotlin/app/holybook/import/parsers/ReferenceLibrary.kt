package app.holybook.import.parsers

import app.holybook.api.models.Paragraph
import app.holybook.import.BookContent
import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.Names
import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.textRecursively

object ReferenceLibrary {

    val parser: ParagraphParser = XmlParser("bahai.org") {
        return@XmlParser parse()
    }

    private fun Konsumer.parse(): BookContent {
        var title = ""
        var author = ""
        val paragraphs = mutableListOf<Paragraph>()
        child("html") {
            child("head") {
                allChildrenAutoIgnore(Names.of("meta", "title")) {
                    when (localName) {
                        "meta" -> if (attributes.getValueOrNull("name") == "author") {
                            author = attributes.getValue("content")
                        }

                        "title" -> text()
                    }
                }
            }
            child("body") {
                child("div") {
                    child("div") {
                        child("div") {
                            child("div") {
                                children("p") {
                                    paragraphs.add(
                                        Paragraph(
                                            paragraphs.size,
                                            textRecursively(),
                                            "body"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                skipContents()
            }
        }
        return BookContent(title, author, paragraphs)
    }
}