package app.holybook.lib.parsers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

fun <T> ByteArray.parse(body: Document.() -> T): T {
  val document = Jsoup.parse(String(this))
  return body(document)
}
