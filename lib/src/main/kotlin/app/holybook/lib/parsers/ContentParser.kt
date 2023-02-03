package app.holybook.import.common

import io.ktor.http.*

/** Abstract type for parsing a file into a data type. */
typealias ContentParser<T> = (ByteArray) -> T

class ContentParsingRule<T>(val matcher: ContentMatcher, val parser: ContentParser<T>)
