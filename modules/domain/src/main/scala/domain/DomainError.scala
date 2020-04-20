package domain

sealed trait DomainError {
  val errorCode: String
  val message: String
}

case class ValidationError(resource: String, override val message: String) extends DomainError {
  override val errorCode = "validation-error"
}

case class ResourceNotFound(resource: String, condition: Any) extends DomainError {
  override val errorCode = "validation-error"
  override val message   = s"$resource ($condition) is not found"
}
