package app.holybook.eval

import app.holybook.lib.translation.ModelConfiguration
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [TranslatorModule::class])
interface EvalComponent {
  fun translatorRegistry(): TranslatorRegistry

  @Component.Builder
  interface Builder {
    @BindsInstance fun modelConfiguration(modelConfiguration: ModelConfiguration): Builder

    fun build(): EvalComponent
  }
}
