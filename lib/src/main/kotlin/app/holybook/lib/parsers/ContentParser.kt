package app.holybook.lib.parsers

import app.holybook.import.common.ContentMatcher

/** Abstract type for parsing a file into a data type. */
typealias ContentParser<T> = (ByteArray) -> T

class ContentParsingRule<T>(val matcher: ContentMatcher, val parser: ContentParser<T>)
