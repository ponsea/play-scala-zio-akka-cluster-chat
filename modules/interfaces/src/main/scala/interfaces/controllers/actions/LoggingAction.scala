package interfaces.controllers.actions

import domain.utils.Logger
import play.api.mvc.Result
import zio.{ Has, ZIO, ZLayer }

class LoggingAction(logger: Logger) extends ZioActionFunction[Any, Has[Unit]] {

  override def invokeBlock[A, Env1, AT1 <: Has[_]](
    request: RequestWithAttachments[A, AT1],
    block: RequestWithAttachments[A, Has[Unit] with AT1] => ZIO[Env1, Throwable, Result]
  ): ZIO[Env1, Throwable, Result] = {
    for {
      _      <- logger.info(request.toString)
      result <- block(request.attachNone)
      _      <- logger.info(result.toString)
    } yield result
  }
}

object LoggingAction {
  val layer = ZLayer.fromService(new LoggingAction(_))
}
