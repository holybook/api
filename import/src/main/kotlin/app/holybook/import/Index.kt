package app.holybook.import

import app.holybook.import.model.ContentDescriptor
import kotlinx.coroutines.flow.Flow

suspend fun fetchAndImportIndex(descriptors: Flow<ContentDescriptor>) {
  descriptors.collect { fetchAndImport(it) }
}
