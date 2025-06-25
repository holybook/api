package app.holybook.lib.translation

import app.holybook.lib.db.getLanguageConfiguration
import app.holybook.lib.translation.TranslateResponseExt.bookId

object TranslationTemplateEngine {

  // Compiled regex pattern to match ${varName} template variables
  private val templateVariablePattern = Regex("""\$\{([^}]+)}""")

  fun renderPrompt(model: TranslationTemplateData): String {
    val templateContent = loadTemplateFromResources("translation_prompt.kte")
    val variables = mapOf(
      "fromLanguage" to getLanguageConfiguration(model.fromLanguage),
      "toLanguage" to getLanguageConfiguration(model.toLanguage),
      "authoritativeTranslations" to model.authoritativeTranslationString(),
      "paragraphs" to model.paragraphsString()
    )
    return replaceTemplateVariables(templateContent, variables)
  }

  private fun TranslationTemplateData.authoritativeTranslationString() =
    authoritativeTranslations.joinToString("\n") { translation ->
      """"
      <p bookId="${translation.bookId}" index="${translation.translatedParagraph.index}">
        ${translation.translatedParagraph.text}
      </p>
      """".trimIndent()
    }

  private fun TranslationTemplateData.paragraphsString() =
    paragraphsToBeTranslated.joinToString("\n") { paragraph ->
      """"
      <p bookId="${paragraph.reference?.bookId}" index="${paragraph.reference?.index}">
         ${paragraph.text}
      </p>
      """".trimIndent()
    }

  private fun loadTemplateFromResources(fileName: String): String {
    return this::class.java.classLoader.getResourceAsStream(fileName)
      ?.use { inputStream ->
        inputStream.bufferedReader().readText()
      }
      ?: throw IllegalArgumentException("Template file '$fileName' not found in resources")
  }

  /**
   * Replaces all occurrences of ${varName} in the template with the corresponding value
   * from the variables map at key 'varName'.
   *
   * @param template The string template containing ${varName} placeholders
   * @param variables Map from variable names to their replacement values
   * @return The template with all variables replaced
   */
  fun replaceTemplateVariables(
    template: String,
    variables: Map<String, String>
  ): String {
    return templateVariablePattern.replace(template) { matchResult ->
      val variableName = matchResult.groupValues[1]
      variables[variableName]
        ?: matchResult.value // Keep original if variable not found
    }
  }

}
