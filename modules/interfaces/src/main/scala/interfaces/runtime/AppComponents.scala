package interfaces.runtime

import controllers.AssetsComponents
import interfaces.controllers.{ ConversationController, SendCommentController }
import play.api.ApplicationLoader.Context
import play.api.BuiltInComponentsFromContext
import play.api.http.{ DefaultHttpErrorHandler, HtmlOrJsonHttpErrorHandler, JsonHttpErrorHandler }
import play.filters.HttpFiltersComponents
import router.Routes
import zio.Runtime
import zio.internal.Platform

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    // with ClusterShardingComponents
    // with SlickComponents
    with HttpFiltersComponents
    with AssetsComponents {
  override lazy val httpErrorHandler =
    new HtmlOrJsonHttpErrorHandler(
      new DefaultHttpErrorHandler(environment, configuration, devContext.map(_.sourceMapper), Some(router)),
      new JsonHttpErrorHandler(environment, devContext.map(_.sourceMapper))
    )

  val layers = new AppLayers(this)

  val runtime = Runtime.unsafeFromLayer(
    layers.sendCommentController ++
    layers.conversationControllerProvider,
    Platform.fromExecutionContext(executionContext)
  )

  val router = new Routes(
    httpErrorHandler,
    runtime.environment.get[SendCommentController.Provider].apply(runtime),
    runtime.environment.get[ConversationController.Provider].apply(runtime),
    assets
  )
}
