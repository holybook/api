package app.holybook.import

import app.holybook.import.model.ContentDescriptor
import kotlinx.coroutines.flow.Flow

suspend fun fetchAndImportIndex(descriptors: Flow<List<ContentDescriptor>>) {
  descriptors.collect { it.forEach { fetchAndImport(it) } }
}
