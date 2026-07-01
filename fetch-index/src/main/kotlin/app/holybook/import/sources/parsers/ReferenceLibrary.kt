package app.holybook.import.sources.parsers

import app.holybook.lib.models.ContentDescriptor
import app.holybook.lib.parsers.ContentParser
import app.holybook.lib.parsers.parse
import org.jsoup.nodes.Document

object ReferenceLibrary {

  val uhjList: ContentParser<List<ContentDescriptor>> = { it.parse { parseUHJList() } }

  /**
   * Enumerates every work listed on an author's index page (e.g.
   * `/library/authoritative-texts/bahaullah/`). Each work's full text is served as a single xhtml
   * at `<work>/<slug>.xhtml`.
   */
  fun workList(urlAuthor: String, authorCode: String): ContentParser<List<ContentDescriptor>> = {
    it.parse { parseWorkList(urlAuthor, authorCode) }
  }

  private fun Document.parseWorkList(
    urlAuthor: String,
    authorCode: String,
  ): List<ContentDescriptor> {
    val workLink = Regex("^/library/authoritative-texts/$urlAuthor/([^/]+)/$")
    return select("a[href]")
      .map { it.attr("href").removePrefix("https://www.bahai.org").removePrefix("https://bahai.org") }
      .mapNotNull { workLink.find(it)?.groupValues?.get(1) }
      .distinct()
      .map { slug ->
        ContentDescriptor(
          id = "${authorCode}_$slug".take(128),
          language = "en",
          authorCode = authorCode,
          url =
            "https://www.bahai.org/library/authoritative-texts/$urlAuthor/$slug/$slug.xhtml",
        )
      }
  }

  private fun Document.parseUHJList(): List<ContentDescriptor> {
    val ids = select("td.col-date a[href]").eachAttr("href")
    return ids.map {
      val id = it.replace("/1", "")
      ContentDescriptor(
        id = "uhj$id",
        language = "en",
        authorCode = "uhj",
        url =
          "https://bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/${id}/${id}.xhtml",
      )
    }
  }
}
