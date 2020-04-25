package interfaces.persistence.slick

import domain.aggregates.comment.{ Comment, CommentRepository }
import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount
import eu.timepit.refined.auto._
import interfaces.persistence.slick.tables.CommentsTable
import zio._

class CommentRepositoryBySlick(commentsTable: CommentsTable, dbioRunner: DBIORunner) extends CommentRepository {
  import commentsTable.profile.api._

  def findById(id: Comment.Id): IO[Unit, Comment] = {
    val Comment.Id(uuid) = id
    val dbio = commentsTable.query
      .filter(_.id === uuid)
      .result
      .headOption
    dbioRunner
      .runReadonly(dbio).get
      .flatMap(recordToComment)
  }

  def save(comment: Comment): UIO[Unit] = {
    val dbio = commentsTable.query.insertOrUpdate(commentToRecord(comment))
    dbioRunner.run(dbio).unit
  }

  private def commentToRecord(comment: Comment): CommentsTable.Record = {
    import comment._
    CommentsTable.Record(
      id = id.underlying,
      authorId = authorId.underlying,
      content = content.underlying,
      conversationKey = conversationKey.underlying,
      createdAt = createdAt,
      updatedAt = updatedAt
    )
  }

  private def recordToComment(record: CommentsTable.Record): UIO[Comment] = {
    (for {
      content         <- Comment.Content.validated(record.content)
      conversationKey <- Conversation.Key.validated(record.conversationKey)
    } yield {
      Comment(
        id = Comment.Id(record.id),
        authorId = UserAccount.Id(record.authorId),
        content = content,
        conversationKey = conversationKey,
        createdAt = record.createdAt,
        updatedAt = record.updatedAt
      )
    }).orDieWith(error => new IllegalStateException(error.toString))
  }
}

object CommentRepositoryBySlick {
  val layer: URLayer[Has[CommentsTable] with Has[DBIORunner], Has[CommentRepository]] =
    ZLayer.fromServices[CommentsTable, DBIORunner, CommentRepository](
      new CommentRepositoryBySlick(_, _)
    )
}
