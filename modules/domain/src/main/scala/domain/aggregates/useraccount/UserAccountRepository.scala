package domain.aggregates.useraccount

import zio.UIO

trait UserAccountRepository {
  def findById(id: UserAccount.Id): UIO[Option[UserAccount]]
}
