package app.holybook.lib.translation

import javax.inject.Inject

data class ModelConfiguration @Inject constructor(
    val apiKey: String,
    val modelName: String
)