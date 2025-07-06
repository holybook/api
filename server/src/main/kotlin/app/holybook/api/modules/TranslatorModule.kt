package app.holybook.api.modules

import app.holybook.lib.translation.Translator
import app.holybook.lib.translation.MonolithicTranslator
import dagger.Binds
import dagger.Module

@Module
interface TranslatorModule {
    @Binds
    fun bindTranslator(translator: MonolithicTranslator): Translator
}
