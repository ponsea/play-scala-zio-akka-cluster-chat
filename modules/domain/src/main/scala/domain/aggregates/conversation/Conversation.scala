package domain.aggregates.conversation

import domain.ValidationError
import domain.aggregates.conversation.Conversation.Key
import domain.utils.RefinedTypes.NonEmptyStringLess255
import eu.timepit.refined.api.RefType.applyRef
import zio.{ IO, UIO, ZIO }

case class Conversation(key: Key)

object Conversation {
  case class Key(underlying: NonEmptyStringLess255)
  object Key {
    def validated(input: String): IO[ValidationError, Key] = {
      applyRef[NonEmptyStringLess255](input).fold(
        errorMessage => ZIO.fail(ValidationError("conversation.key", errorMessage)),
        valid => UIO(Key(valid))
      )
    }
  }
}
