package app.holybook.import.sources

import app.holybook.import.common.ContentParser
import app.holybook.import.model.ContentDescriptor
import app.holybook.import.parsers.getStringList
import app.holybook.import.parsers.parse
import org.w3c.dom.Node

object ReferenceLibrary {

  val uhjList: ContentParser<List<ContentDescriptor>> = {
    it.parse {
      parseUHJList()
    }
  }

  fun Node.parseUHJList(): List<ContentDescriptor> {
    val ids = getStringList("//td[@class=col-date]/a@href")
    return ids.map {
      ContentDescriptor(
        it.replace("_001/1", ""),
        "en",
        "https://www.bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/$it"
      )
    }
  }
}
