package app.holybook.import.sources

import app.holybook.import.model.ContentDescriptor
import app.holybook.import.parsers.UrlPrefixMatcher
import app.holybook.import.parsers.XmlDOMParser
import app.holybook.import.parsers.getStringList
import org.w3c.dom.Node

object ReferenceLibrary {

  val uhjList =
    XmlDOMParser(
      UrlPrefixMatcher(
        "bahai.org",
        "library/authoritative-texts/the-universal-house-of-justice/messages/"
      ),
      this::parseUHJList
    )

  private fun parseUHJList(rootElement: Node): List<ContentDescriptor> {
    val ids = rootElement.getStringList("//td[@class=col-date]/a@href")
    return ids.map {
      ContentDescriptor(
        it.replace("_001/1", ""),
        "en",
        "https://www.bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/$it"
      )
    }
  }
}
