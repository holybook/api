package app.holybook.api.modules

import app.holybook.lib.translation.ModelConfiguration
import app.holybook.lib.translation.Translator
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [TranslatorModule::class])
interface ServerComponent {
  fun translator(): Translator

  @Component.Builder
  interface Builder {
    @BindsInstance fun modelConfiguration(modelConfiguration: ModelConfiguration): Builder

    fun build(): ServerComponent
  }
}
