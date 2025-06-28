package app.holybook.lib.translation

/**
 * Interface for matching text between original and translation.
 * 
 * This interface is used by IncrementalTranslator to find the best matching
 * substring in a reference translation that corresponds to a given paragraph.
 */
interface TextMatcher {
  
  /**
   * Finds the best matching substring in the reference text that corresponds
   * to the given paragraph text.
   * 
   * @param paragraphText The original paragraph text to match
   * @param referenceText The reference translation text to search within
   * @return The best matching substring from the reference text, or null if no good match is found
   */
  fun findBestMatch(
    paragraphText: String,
    referenceText: String
  ): String?
} 