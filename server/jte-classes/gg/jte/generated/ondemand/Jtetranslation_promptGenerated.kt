@file:Suppress("ktlint")
package gg.jte.generated.ondemand
import app.holybook.lib.translation.TranslationTemplateData
import app.holybook.lib.models.TranslateResponse
@Suppress("UNCHECKED_CAST", "UNUSED_PARAMETER")
class Jtetranslation_promptGenerated {
companion object {
	@JvmField val JTE_NAME = "translation_prompt.kte"
	@JvmField val JTE_LINE_INFO = intArrayOf(0,0,0,1,3,3,3,3,3,5,5,5,6,6,6,6,6,6,6,6,9,9,9,9,3,3,3,3,3)
	@JvmStatic fun render(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, model:TranslationTemplateData) {
		jteOutput.writeContent("\n")
		for (response in model.translateResponses) {
			jteOutput.writeContent("\n    <p id=\"")
			jteOutput.setContext("p", "id")
			jteOutput.writeUserContent(response.allOriginalResults[0].bookId)
			jteOutput.setContext("p", null)
			jteOutput.writeContent(":")
			jteOutput.setContext("p", "id")
			jteOutput.writeUserContent(response.translatedParagraph.index)
			jteOutput.setContext("p", null)
			jteOutput.writeContent("\">\n\n    </p>\n")
		}
	}
	@JvmStatic fun renderMap(jteOutput:gg.jte.html.HtmlTemplateOutput, jteHtmlInterceptor:gg.jte.html.HtmlInterceptor?, params:Map<String, Any?>) {
		val model = params["model"] as TranslationTemplateData
		render(jteOutput, jteHtmlInterceptor, model);
	}
}
}
