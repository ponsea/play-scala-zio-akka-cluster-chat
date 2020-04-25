package interfaces.persistence.slick.tables

import java.time.Instant
import java.util.UUID

import slick.jdbc.JdbcProfile
import zio.{ Has, URLayer, ZLayer }

class CommentsTable(val profile: JdbcProfile) {
  import CommentsTable.Record
  import profile.api._

  class Schema(tag: Tag) extends Table[Record](tag, "comments") {
    def id              = column[UUID]("id", O.PrimaryKey)
    def authorId        = column[String]("author_id", O.Length(254))
    def content         = column[String]("content", O.Length(254))
    def conversationKey = column[String]("conversation_key", O.Length(254))
    def createdAt       = column[Instant]("created_at", O.Length(40))
    def updatedAt       = column[Instant]("updated_at", O.Length(40))

    def * =
      (id, authorId, content, conversationKey, createdAt, updatedAt) <> (Record.tupled, Record.unapply)
  }

  val query = TableQuery[Schema]
}

object CommentsTable {
  case class Record(
    id: UUID,
    authorId: String,
    content: String,
    conversationKey: String,
    createdAt: Instant,
    updatedAt: Instant
  )

  val layer: URLayer[Has[JdbcProfile], Has[CommentsTable]] =
    ZLayer.fromService(new CommentsTable(_))
}
