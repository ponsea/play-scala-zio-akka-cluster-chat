package interfaces.controllers

import domain.DomainError
import domain.auth.UserCredential
import domain.usecases.SendCommentUseCase
import interfaces.controllers.actions.{ AuthAction, LoggingAction }
import interfaces.json.CommentJsonWrites
import play.api.libs.json.Json
import play.api.mvc._
import zio.{ IO, Runtime, UIO, ZLayer }

case class SendCommentController(
  cc: ControllerComponents,
  loggingAction: LoggingAction,
  authAction: AuthAction,
  interactor: SendCommentUseCase.Interactor,
  errorHandler: ErrorHandler
)(rt: Runtime[Any])
    extends ZioAbstractController(cc)(rt)
    with CommentJsonWrites {

  import SendCommentUseCase._

  implicit val inputReads = Json.reads[Input]

  def invoke = {
    ZioAction andThen
    // loggingAction andThen
    authAction
  }.unsafeRun(parse.json[Input]) { request =>
    val credential = request.attachments.get[UserCredential]
    val input      = request.body
    val result     = interactor.execute(input)(credential)

    result.foldCauseM(
      cause => errorHandler.handleErrors(cause),
      output => UIO(Created(Json.toJson(output.createdComment)))
    )
  }
}

object SendCommentController {
  type Provider = Runtime[Any] => SendCommentController

  object Provider {
    val layer = ZLayer.fromServices(apply _)
  }
}
