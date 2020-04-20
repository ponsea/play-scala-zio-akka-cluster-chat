package interfaces

import akka.util.ByteString
import interfaces.controllers.actions.{ RequestWithAttachments, ZioActionBuilder }
import play.api.libs.json.Reads
import play.api.libs.streams.Accumulator
import play.api.mvc._
import zio.{ Has, Runtime, ZIO }

package object controllers {
  abstract class ZioAbstractController[Env](protected val controllerComponents: ControllerComponents)(
    implicit val runtime: Runtime[Env]
  ) extends BaseController {

    val ZioAction: ZioActionBuilder[AnyContent, Any, Has[Unit]] =
      DefaultZioActionBuilder(parse.default)
  }

  object DefaultZioActionBuilder {
    def apply(parser: BodyParser[AnyContent]): ZioActionBuilder[AnyContent, Any, Has[Unit]] =
      new ZioActionBuilderImpl[AnyContent](parser)
  }

  class ZioActionBuilderImpl[B](val parser: BodyParser[B]) extends ZioActionBuilder[B, Any, Has[Unit]] {
    override def invokeBlock[A, Env1, AT1 <: Has[_]](
      request: RequestWithAttachments[A, AT1],
      block: RequestWithAttachments[A, Has[Unit] with AT1] => ZIO[Env1, Throwable, Result]
    ): ZIO[Env1, Throwable, Result] = block(request.attachNone)
  }
}
