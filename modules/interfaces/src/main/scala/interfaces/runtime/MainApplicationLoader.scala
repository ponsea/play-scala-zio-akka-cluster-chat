package interfaces.runtime

import play.api.ApplicationLoader.Context
import play.api.{ Application, ApplicationLoader, LoggerConfigurator }

class MainApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new AppComponents(context).application
  }
}
