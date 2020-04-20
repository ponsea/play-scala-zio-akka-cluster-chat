package interfaces.controllers

import akka.NotUsed
import akka.stream.scaladsl._
import domain.DomainError
import domain.usecases.WatchConversationUpdatesUseCase
import domain.utils.Logger
import interfaces.json.ConversationUpdateJsonWrites
import play.api.libs.json.{ JsValue, Json }
import play.api.mvc._
import zio.interop.reactivestreams._
import zio.{ IO, Runtime, ZIO, ZLayer }

case class ConversationController(
  cc: ControllerComponents,
  interactor: WatchConversationUpdatesUseCase.Interactor,
  logger: Logger,
  errorHandler: ErrorHandler
)(runtime: Runtime[Any])
    extends AbstractController(cc)
    with ConversationUpdateJsonWrites {

  import WatchConversationUpdatesUseCase._

  def watchUpdates(conversationKey: String): WebSocket =
    WebSocket.acceptOrResult[Any, JsValue] { _ =>
      val input  = Input(conversationKey)
      val result = interactor.execute(input)

      runtime.unsafeRunToFuture(render(result))
    }

  private def render(
    result: IO[DomainError, Output]
  ): ZIO[Any, Nothing, Either[Result, Flow[Any, JsValue, NotUsed]]] = {
    result.foldCauseM(
      cause =>
        errorHandler
          .handleErrors(cause)
          .map(Left(_)),
      output =>
        output.conversationUpdates
          .map(Json.toJson(_))
          .toPublisher
          .map(Source.fromPublisher)
          .map(Flow.fromSinkAndSource(Sink.ignore, _))
          .map(Right(_))
    )
  }
}

object ConversationController {
  type Provider = Runtime[Any] => ConversationController
  object Provider {
    val layer = ZLayer.fromServices(apply _)
  }
}
