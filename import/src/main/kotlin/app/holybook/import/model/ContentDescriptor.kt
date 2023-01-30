package app.holybook.import.model

import kotlinx.serialization.Serializable

/** Describes the location of an original document containing the data of a book. */
@Serializable data class ContentDescriptor(val id: String, val language: String, val url: String)
