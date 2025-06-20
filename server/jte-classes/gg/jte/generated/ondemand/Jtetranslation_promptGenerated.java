package gg.jte.generated.ondemand;
@SuppressWarnings("unchecked")
public final class Jtetranslation_promptGenerated {
	public static final String JTE_NAME = "translation_prompt.jte";
	public static final int[] JTE_LINE_INFO = {5,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,9,9,9,9,9,9,9};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor) {
		jteOutput.writeContent("import app.holybook.lib.translation.TranslationTemplateData\nimport app.holybook.lib.models.TranslateResponse\n\n@Param TranslationTemplateData model\n\n");
		for (TranslateResponse response : model.translateResponses) {
			jteOutput.writeContent("\n    <p id=\"");
			jteOutput.setContext("p", "id");
			jteOutput.writeUserContent(response.allOriginalResults.get(0).bookId);
			jteOutput.setContext("p", null);
			jteOutput.writeContent(":");
			jteOutput.setContext("p", "id");
			jteOutput.writeUserContent(response.translatedParagraph.index);
			jteOutput.setContext("p", null);
			jteOutput.writeContent("\">\n\n    </p>\n");
		}
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		render(jteOutput, jteHtmlInterceptor);
	}
}
