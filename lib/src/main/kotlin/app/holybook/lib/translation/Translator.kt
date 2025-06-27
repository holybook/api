package app.holybook.lib.translation

/**
 * Interface for translation services that can translate text using AI models.
 * 
 * Implementations should handle the translation of text from one language to another
 * using various AI translation models or services.
 */
interface Translator {
  
  /**
   * Translates text using the provided translation model request.
   * 
   * @param request The translation request containing source language, target language,
   *                and paragraphs to be translated with optional reference translations
   * @return The translation response containing translated paragraphs, or null if translation fails
   */
  fun translate(request: TranslationModelRequest): TranslationModelResponse?
}
