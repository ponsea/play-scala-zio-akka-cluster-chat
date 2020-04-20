package domain.aggregates.comment

import java.time.Instant

import domain.DomainEvent
import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount

sealed trait CommentEvent extends DomainEvent {
  val commentId: Comment.Id
  val authorId: UserAccount.Id
  val conversationKey: Conversation.Key
}

object CommentEvent {
  case class Sent(
    commentId: Comment.Id,
    authorId: UserAccount.Id,
    conversationKey: Conversation.Key,
    occurredAt: Instant
  ) extends CommentEvent

  case class Modified(
    commentId: Comment.Id,
    authorId: UserAccount.Id,
    conversationKey: Conversation.Key,
    occurredAt: Instant
  ) extends CommentEvent

  case class Removed(
    commentId: Comment.Id,
    authorId: UserAccount.Id,
    conversationKey: Conversation.Key,
    occurredAt: Instant
  ) extends CommentEvent
}
