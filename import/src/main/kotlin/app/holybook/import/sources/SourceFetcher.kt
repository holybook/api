package app.holybook.import.sources

import app.holybook.import.model.ContentDescriptor
import app.holybook.import.parsers.ContentParser
import io.ktor.client.*

val httpClient = HttpClient()

private val fetchers =
  mapOf<String, ContentParser<List<ContentDescriptor>>>(
    "https://www.bahai.org/library/authoritative-texts/the-universal-house-of-justice/messages/" to
      ReferenceLibrary.uhjList
  )

suspend fun fetchSources() {
  fetchers.forEach { (url, parser) ->
    val response = httpClient.get(url)
    parser.parse(response.body())
  }
}

