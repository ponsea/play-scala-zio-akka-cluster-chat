package interfaces.json

import domain.{ DomainError, ResourceNotFound, ValidationError }
import play.api.libs.json.{ JsObject, Json, Writes }

trait DomainErrorJsonWrites {
  implicit private val validationErrorWrites = Writes[ValidationError] { error =>
    commonPartOfError(error).deepMerge(Json.obj("resource" -> error.resource))
  }

  implicit private val resourceNotFoundWrites = Writes[ResourceNotFound] { error =>
    commonPartOfError(error).deepMerge(Json.obj("resource" -> error.resource))
  }

  implicit val domainErrorWrites = Writes[DomainError] {
    case error: ValidationError  => Json.toJson(error)
    case error: ResourceNotFound => Json.toJson(error)
  }

  private def commonPartOfError(error: DomainError): JsObject = Json.obj(
    "errorCode" -> error.errorCode,
    "message"   -> error.message,
  )
}
