package app.holybook.import.parsers

import com.gitlab.mvysny.konsumexml.Konsumer
import com.gitlab.mvysny.konsumexml.konsumeXml
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Node

private val documentBuilderFactory = DocumentBuilderFactory.newInstance()

fun <T> ByteArray.parseWithKonsumer(body: Konsumer.() -> T) = String(this).konsumeXml().body()

fun <T> ByteArray.parse(body: Node.() -> T): T {
  val documentBuilder = documentBuilderFactory.newDocumentBuilder()
  return body(documentBuilder.parse(inputStream()).documentElement)
}
