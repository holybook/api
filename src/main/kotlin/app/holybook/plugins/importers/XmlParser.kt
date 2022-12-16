package app.holybook.plugins.importers

import com.gitlab.mvysny.konsumexml.allChildrenAutoIgnore
import com.gitlab.mvysny.konsumexml.konsumeXml
import com.gitlab.mvysny.konsumexml.Names

class XmlParser : ParagraphParser {
    override val contentType: String
        get() = "application/xml"

    override fun parse(content: ByteArray): List<String> {
        String(content).konsumeXml().apply {
            child("doc") {
                child("metadata") {
                    allChildrenAutoIgnore(Names.of("title", "description")) {
                        when (localName) {
                            "titleName" -> println(text())
                        }
                    }
                }
            }
        }
        return listOf()
    }


}