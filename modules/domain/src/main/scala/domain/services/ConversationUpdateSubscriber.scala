package domain.services

import java.time.Instant

import domain.aggregates.comment.{ Comment, CommentEvent }
import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount
import domain.{ DomainEvent, DomainEventBus }
import zio.stream.{ Stream, ZStream }
import zio.{ Queue, UIO, ZLayer }

sealed trait ConversationUpdate {
  val conversationKey: Conversation.Key
  val occuredAt: Instant
}

object ConversationUpdate {
  case class CommentAdded(
    commentId: Comment.Id,
    commentContent: Comment.Content,
    authorId: UserAccount.Id,
    conversationKey: Conversation.Key,
    occuredAt: Instant
  ) extends ConversationUpdate

  case class CommentUpdated(
    commentId: Comment.Id,
    authorId: UserAccount.Id,
    conversationKey: Conversation.Key,
    occuredAt: Instant
  ) extends ConversationUpdate

  case class CommentDeleted(
    commentId: Comment.Id,
    authorId: UserAccount.Id,
    conversationKey: Conversation.Key,
    occuredAt: Instant
  ) extends ConversationUpdate
}

class ConversationUpdateSubscriber(domainEventBus: DomainEventBus) {
  def subscribeByConversationKey(
    targetConversationKey: Conversation.Key
  ): UIO[Stream[Nothing, ConversationUpdate]] = {
    // 関心のあるドメインイベントを拾ってクライアントへの通知用のStreamに変換
    domainEventBus.subscribe().map { domainEvents: Queue[DomainEvent] =>
      ZStream // Streamへ変換。コンシューマーの終了時はQueueはシャットダウンされる
        .fromQueueWithShutdown(domainEvents)
        .collect {
          case CommentEvent.Sent(commentId, commentContent, authorId, conversationKey, occurredAt) =>
            ConversationUpdate.CommentAdded(commentId, commentContent, authorId, conversationKey, occurredAt)
          case CommentEvent.Modified(commentId, authorId, conversationKey, occurredAt) =>
            ConversationUpdate.CommentUpdated(commentId, authorId, conversationKey, occurredAt)
          case CommentEvent.Removed(commentId, authorId, conversationKey, occurredAt) =>
            ConversationUpdate.CommentDeleted(commentId, authorId, conversationKey, occurredAt)
        }
        .filter(_.conversationKey == targetConversationKey)
        // クライアントの処理が遅くバックプレッシャーがかかった場合は16件までバッファリングし、
        // 超過した場合は古いものから削除する
        .bufferSliding(16)
    }
  }
}

object ConversationUpdateSubscriber {
  val layer = ZLayer.fromService(new ConversationUpdateSubscriber(_))
}
