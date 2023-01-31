package app.holybook.import.parsers

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.konsumeXml
import javax.xml.parsers.DocumentBuilderFactory
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.w3c.dom.Node

private val documentBuilderFactory =
  DocumentBuilderFactory.newInstance().apply {
    isValidating = false
    isNamespaceAware = true
    isIgnoringComments = false
    isIgnoringElementContentWhitespace = false
    isExpandEntityReferences = false
  }

fun <T> ByteArray.parseWithKonsumer(body: Konsumer.() -> T) = String(this).konsumeXml().body()

fun <T> ByteArray.parse(body: Document.() -> T): T {
  val document = Jsoup.parse(String(this))
  return body(document)
}
