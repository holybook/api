package app.holybook.import.sources.parsers

import app.holybook.lib.models.ContentDescriptor
import app.holybook.lib.parsers.ContentParser
import app.holybook.lib.parsers.parse
import org.jsoup.nodes.Document

object ReferenceLibrary {

  val uhjList: ContentParser<List<ContentDescriptor>> = { it.parse { parseUHJList() } }

  private fun Document.parseUHJList(): List<ContentDescriptor> {
    val ids = select("td.col-date a[href]").eachAttr("href")
    return ids.map {
      val id = it.replace("/1", "")
      ContentDescriptor(
        id = "uhj$id",
        language = "en",
        authorCode = "uhj",
        url =
          "https://bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/${id}/${id}.xhtml"
      )
    }
  }
}
