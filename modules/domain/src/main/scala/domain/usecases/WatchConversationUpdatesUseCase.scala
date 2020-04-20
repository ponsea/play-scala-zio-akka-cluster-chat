package domain.usecases

import domain.DomainError
import domain.aggregates.conversation.Conversation
import domain.services.{ ConversationUpdate, ConversationUpdateSubscriber }
import zio.stream.Stream
import zio.{ IO, ZLayer }

object WatchConversationUpdatesUseCase {
  case class Input(conversationKey: String)
  case class Output(conversationUpdates: Stream[Nothing, ConversationUpdate])

  class Interactor(subscriber: ConversationUpdateSubscriber) {
    def execute(input: Input): IO[DomainError, Output] = {
      for {
        conversationKey <- Conversation.Key.validated(input.conversationKey)
        updates         <- subscriber.subscribeByConversationKey(conversationKey)
      } yield Output(updates)
    }
  }

  object Interactor {
    val layer = ZLayer.fromService(new Interactor(_))
  }
}
