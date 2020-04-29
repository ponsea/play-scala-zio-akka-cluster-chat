package interfaces.controllers

import domain.utils.Logger
import domain.{ DomainError, ResourceNotFound, ValidationError }
import interfaces.json.DomainErrorJsonWrites
import play.api.http.Status
import play.api.libs.json.{ JsNull, JsObject, JsValue, Json }
import play.api.mvc.{ Result, Results }
import zio.{ Cause, UIO, ZIO, ZLayer }

class ErrorHandler(logger: Logger) extends Results with Status with DomainErrorJsonWrites {
  def handleErrors(cause: Cause[DomainError]): UIO[Result] = {
    for {
      // 復帰不可能なシステムエラー(`Cause#defects`)をログ出力
      _ <- ZIO.foreach(cause.defects)(logger.throwable("Non-recoverable error occurred.", _))
      result = (cause.failures, cause.defects) match { // ここのfailuresはList[DomainError]型
        // failures(バリデーションエラーなど)がある場合は適切なHTTPエラーレスポンスを返す
        case (fail :: _, _) => Status(statusCodeOf(fail))(errorsToJson(cause.failures))
        // `defects`(復帰不可なシステムエラー)のみの場合はInternalServerエラー
        case _ => InternalServerError
      }
    } yield result
  }

  private def errorsToJson(errors: List[DomainError]): JsValue = {
    (for {
      head      <- errors.headOption
      headJsObj <- Json.toJson(head).asOpt[JsObject]
    } yield headJsObj.deepMerge(Json.obj("errors" -> errors))).getOrElse(JsNull)
  }

  private def statusCodeOf(error: DomainError): Int = error match {
    case _: ValidationError  => BAD_REQUEST
    case _: ResourceNotFound => NOT_FOUND
  }
}

object ErrorHandler {
  val layer = ZLayer.fromService(new ErrorHandler(_))
}
