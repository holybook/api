package app.holybook.import.sources.parsers

import app.holybook.lib.models.ContentDescriptor
import app.holybook.lib.parsers.ContentParser
import app.holybook.lib.parsers.parse
import org.jsoup.nodes.Document

object BibliothekBahaiDe {

  val uhjList: ContentParser<List<ContentDescriptor>> = { it.parse { parseUHJList() } }

  private fun Document.parseUHJList(): List<ContentDescriptor> {
    val ids = select("div#divMessageList a[href]").eachAttr("href")
    return ids.filter { it.contains("uhj-muhj") }.map {
      val dateString = it.extractDateString()
      val dateStringWithHyphens =
        dateString.substring(0, 4) +
          "-" +
          dateString.substring(4, 6) +
          "-" +
          dateString.substring(6, 8)
      ContentDescriptor(
        id = "uhj${dateString.normalizeDay()}_001",
        language = "de",
        authorCode = "uhj",
        url =
          "https://bibliothek.bahai.de/download/xml/export_UHJ_${dateStringWithHyphens}_de%20(1.01)%20MAS-XML.xml",
      )
    }
  }

  private fun String.extractDateString(): String {
    return replace("pubReader.php?titleLangUri=uhj-muhj", "").replace("-de", "")
  }

  /**
   * Messages with an unknown day are dated `YYYY-MM-00` by the source. The id must use day `01` to
   * stay aligned with the English (bahai.org) ids of the same message, and `00` is not a valid day
   * for the date parsing done downstream. The source URL keeps the original `00` so the file still
   * resolves.
   */
  private fun String.normalizeDay(): String =
    if (substring(6, 8) == "00") substring(0, 6) + "01" else this
}
