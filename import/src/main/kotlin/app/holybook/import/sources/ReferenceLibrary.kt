package app.holybook.import.sources

import app.holybook.import.common.ContentParser
import app.holybook.import.model.ContentDescriptor
import app.holybook.import.parsers.parse
import org.jsoup.nodes.Document

object ReferenceLibrary {

  val uhjList: ContentParser<List<ContentDescriptor>> = { it.parse { parseUHJList() } }

  private fun Document.parseUHJList(): List<ContentDescriptor> {
    val ids = select("td.col-date a[href]").eachAttr("href")
    return ids.map {
      val id = it.replace("/1", "")
      ContentDescriptor(
        id,
        "en",
        "https://bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/${id}/${id}.xhtml"
      )
    }
  }
}
