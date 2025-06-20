package app.holybook.lib.translation

import gg.jte.ContentType
import gg.jte.TemplateEngine
import gg.jte.output.StringOutput
import gg.jte.resolve.ResourceCodeResolver

object TranslationTemplateEngine {
  private val templateEngine = TemplateEngine.create(ResourceCodeResolver(""), ContentType.Html)

  fun renderPrompt(model: TranslationTemplateData): String {
    val output = StringOutput()
    templateEngine.render("translation_prompt.kte", model, output)
    return output.toString()
  }
}
