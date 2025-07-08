package app.holybook.api.modules

import app.holybook.lib.translation.MonolithicTranslator
import app.holybook.lib.translation.Translator
import dagger.Binds
import dagger.Module

@Module
interface TranslatorModule {
  @Binds fun bindTranslator(translator: MonolithicTranslator): Translator
}
