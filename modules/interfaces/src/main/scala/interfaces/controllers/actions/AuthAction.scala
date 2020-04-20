package interfaces.controllers.actions

import domain.aggregates.useraccount.UserAccount
import domain.auth.UserCredential
import play.api.http.HeaderNames
import play.api.mvc.{ Result, Results }
import zio.{ Has, UIO, ZIO, ZLayer }

class AuthAction extends ZioActionRefiner[Any, Has[UserCredential]] with HeaderNames with Results {
  private val headerTokenRegex = """Bearer (.+?)""".r

  override protected def refine[A, AT1 <: Has[_]](
    request: RequestWithAttachments[A, AT1]
  ): ZIO[Any, Result, RequestWithAttachments[A, Has[UserCredential] with AT1]] = {
    request.headers.get(AUTHORIZATION) match {
      case Some(headerTokenRegex(value)) =>
        UIO(request.attach(UserCredential(UserAccount.Id(value)))) // TODO: 認証する。
      case _ => ZIO.fail(Unauthorized)
    }
  }
}

object AuthAction {
  val layer = ZLayer.succeed(new AuthAction)
}
