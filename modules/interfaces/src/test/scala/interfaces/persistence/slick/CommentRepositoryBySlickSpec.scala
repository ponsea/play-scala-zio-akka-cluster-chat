package interfaces.persistence.slick

import java.time.Instant
import java.util.UUID

import domain.aggregates.comment.Comment
import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount
import eu.timepit.refined.auto._
import interfaces.persistence.slick.tables.CommentsTable
import org.scalatest.{ FlatSpec, Matchers }
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import zio._

class CommentRepositoryBySlickSpec extends FlatSpec with Matchers {
  val runtime      = Runtime.default
  val now: Instant = Instant.parse("2000-01-01T00:00:00Z")
  val uuid: UUID   = UUID.fromString("811d4442-c9fc-4b50-9725-843e2f2e6b77")

  val dbConfig          = DatabaseConfig.forConfig[JdbcProfile]("slick.dbs.test")
  val commentsTable     = new CommentsTable(dbConfig.profile)
  val dbioRunner        = new DBIORunner(DatabasePair(dbConfig.db))
  val commentRepository = new CommentRepositoryBySlick(commentsTable, dbioRunner)

  override def withFixture(test: NoArgTest) = {
    import dbConfig.profile.api._
    runtime.unsafeRun(dbioRunner.run(commentsTable.query.schema.create))
    try {
      super.withFixture(test)
    } finally {
      runtime.unsafeRun(dbioRunner.run(commentsTable.query.schema.drop))
    }
  }

  "CommentRepository#save,findById" should "save a comment and then find it by its ID" in {
    val comment = Comment(
      Comment.Id(uuid),
      UserAccount.Id("user-id"),
      Comment.Content("valid comment content"),
      Conversation.Key("valid-conversation-key"),
      now,
      now
    )
    runtime.unsafeRun(commentRepository.save(comment))
    val savedComment = runtime.unsafeRun(commentRepository.findById(Comment.Id(uuid)))
    savedComment shouldBe comment
  }

  "CommentRepository#findById" should "return `ZIO.fail(())` when a not-existing ID is passed" in {
    val result = runtime.unsafeRun(commentRepository.findById(Comment.Id(uuid)).either)
    result shouldBe Left(())
  }
}
