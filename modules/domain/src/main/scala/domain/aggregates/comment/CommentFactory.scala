package domain.aggregates.comment

import java.time.Instant
import java.util.UUID

import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount
import domain.utils.{ CurrentDateTime, IdGenerator }
import zio.{ UIO, ZLayer }

class CommentFactory(
  idGenerator: IdGenerator[UUID],
  currentDateTime: CurrentDateTime[Instant]
) {
  def generate(
    authorId: UserAccount.Id,
    content: Comment.Content,
    conversationKey: Conversation.Key
  ): UIO[Comment] = {
    for {
      uuid <- idGenerator.generate()
      now  <- currentDateTime.get()
    } yield Comment(Comment.Id(uuid), authorId, content, conversationKey, now, now)
  }
}

object CommentFactory {
  val layer = ZLayer.fromServices(new CommentFactory(_, _))
}
