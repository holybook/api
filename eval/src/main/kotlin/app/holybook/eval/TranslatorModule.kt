package app.holybook.eval

import app.holybook.lib.translation.MonolithicTranslator
import app.holybook.lib.translation.ParagraphTranslator
import app.holybook.lib.translation.TextMatcher
import app.holybook.lib.translation.Translator
import app.holybook.lib.translation.impl.GeminiTextMatcher
import app.holybook.lib.translation.impl.GeminiTranslator
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey

@Module
interface TranslatorModule {
  @Binds
  @IntoMap
  @StringKey("monolithic")
  fun bindMonolithicTranslator(impl: MonolithicTranslator): Translator

  @Binds
  @IntoMap
  @StringKey("gemini")
  fun bindGeminiTranslator(impl: GeminiTranslator): ParagraphTranslator

  @Binds
  @IntoMap
  @StringKey("gemini")
  fun bindGeminiTextMatcher(impl: GeminiTextMatcher): TextMatcher
}
