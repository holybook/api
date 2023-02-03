package app.holybook.lib.parsers

import javax.xml.parsers.DocumentBuilderFactory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

private val documentBuilderFactory =
  DocumentBuilderFactory.newInstance().apply {
    isValidating = false
    isNamespaceAware = true
    isIgnoringComments = false
    isIgnoringElementContentWhitespace = false
    isExpandEntityReferences = false
  }

fun <T> ByteArray.parse(body: Document.() -> T): T {
  val document = Jsoup.parse(String(this))
  return body(document)
}
