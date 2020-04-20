package domain.aggregates.useraccount

import java.time.Instant

case class UserAccount(
  id: UserAccount.Id,
  name: UserAccount.Name,
  email: UserAccount.Email,
  createdAt: Instant,
  updatedAt: Instant
)

object UserAccount {
  case class Id(underlying: String)    extends AnyVal
  case class Name(underlying: String)  extends AnyVal
  case class Email(underlying: String) extends AnyVal
}
