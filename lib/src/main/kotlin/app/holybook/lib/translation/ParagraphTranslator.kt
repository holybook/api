package app.holybook.lib.translation

/**
 * Interface for translating individual paragraphs from scratch.
 * 
 * This interface is used by IncrementalTranslator to translate paragraphs
 * that don't have reference translations available.
 */
interface ParagraphTranslator {
  
  /**
   * Translates a single paragraph from the source language to the target language.
   * 
   * @param fromLanguage The source language code
   * @param toLanguage The target language code
   * @param paragraphText The text of the paragraph to translate
   * @return The translated text, or null if translation fails
   */
  fun translateParagraph(
    fromLanguage: String,
    toLanguage: String,
    paragraphText: String
  ): String?
} 