package domain.auth

import domain.aggregates.useraccount.UserAccount

case class UserCredential(userId: UserAccount.Id)
