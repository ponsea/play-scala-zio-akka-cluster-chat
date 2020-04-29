package domain

sealed trait DomainError {
  val errorCode: String
  val message: String
}

// システム上の復帰不可なエラー型は定義しない、例えば
// `case class SystemError(throwable: Throwable)`
// こういったエラーは、ZIO値の`Cause`内にある`defects`として表現する

case class ValidationError(resource: String, override val message: String) extends DomainError {
  override val errorCode = "validation-error"
}

case class ResourceNotFound(resource: String, condition: Any) extends DomainError {
  override val errorCode = "validation-error"
  override val message   = s"$resource ($condition) is not found"
}
