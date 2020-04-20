package domain.aggregates.comment

import java.time.Instant
import java.util.UUID

import domain.ValidationError
import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount
import domain.utils.RefinedTypes.NonEmptyStringLess255
import eu.timepit.refined.api.RefType.applyRef
import zio.{ IO, UIO, ZIO }

case class Comment(
  id: Comment.Id,
  authorId: UserAccount.Id,
  content: Comment.Content,
  conversationKey: Conversation.Key,
  createdAt: Instant,
  updatedAt: Instant
)

object Comment {
  case class Id(underlying: UUID)

  case class Content(underlying: NonEmptyStringLess255)

  object Content {
    def validated(input: String): IO[ValidationError, Content] = {
      applyRef[NonEmptyStringLess255](input).fold(
        errorMessage => ZIO.fail(ValidationError("comment.content", errorMessage)),
        valid => UIO(Content(valid))
      )
    }
  }
}
