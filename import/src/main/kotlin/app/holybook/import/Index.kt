@file:OptIn(ExperimentalSerializationApi::class)

package app.holybook.import

import app.holybook.import.model.ContentDescriptor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

suspend fun fetchAndImportIndex(descriptors: Flow<ContentDescriptor>) {
  descriptors.collect { fetchAndImport(it) }
}
