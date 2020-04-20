package domain.usecases

import java.time.Instant
import java.util.UUID

import domain.ValidationError
import domain.aggregates.comment.{ Comment, CommentEvent, CommentFactory }
import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount
import domain.auth.UserCredential
import eu.timepit.refined.auto._
import mocks.{ CommentRepositoryMock, DomainEventBusMock }
import stubs.{ CurrentDateTimeStub, IdGeneratorStub }
import zio._
import zio.test.Assertion._
import zio.test._
import zio.test.mock.Expectation._

object SendCommentUseCaseSpec extends DefaultRunnableSpec {
  import SendCommentUseCase._

  val now: Instant = Instant.parse("2000-01-01T00:00:00Z")
  val uuid: UUID   = UUID.fromString("811d4442-c9fc-4b50-9725-843e2f2e6b77")

  val mockedCommentFactoryLayer: ULayer[Has[CommentFactory]] = {
    CurrentDateTimeStub.instantLayer(now) ++ IdGeneratorStub.uuidLayer(uuid)
  } >>> CommentFactory.layer

  def spec = suite("SendCommentUseCaseSpec")(
    testM("save a comment and publish a domain event when input is valid") {
      val userId = UserAccount.Id("user-id")

      val comment =
        Comment(
          Comment.Id(uuid),
          userId,
          Comment.Content("valid comment content"),
          Conversation.Key("valid-conversation-key"),
          now,
          now
        )
      val commentSentEvent = CommentEvent.Sent(comment.id, comment.authorId, comment.conversationKey, now)
      val mockedInteractorLayer = {
        CurrentDateTimeStub.instantLayer(now) ++
        mockedCommentFactoryLayer ++
        (CommentRepositoryMock.Save(equalTo(comment)) returns unit) ++
        (DomainEventBusMock.Publish(equalTo(commentSentEvent)) returns unit)
      } >>> Interactor.layer

      (for {
        interactor <- ZIO.access[Has[Interactor]](_.get)
        output     <- interactor.execute(Input("valid comment content", "valid-conversation-key"))(UserCredential(userId))
      } yield {
        assert(output)(equalTo(Output(comment)))
      }).provideLayer(mockedInteractorLayer)
    },
    testM("occur validation errors when input is invalid") {
      val mockedInteractorLayer = {
        CurrentDateTimeStub.instantLayer(now) ++
        mockedCommentFactoryLayer ++
        CommentRepositoryMock.noImplLayer ++
        DomainEventBusMock.noImplLayer
      } >>> Interactor.layer

      (for {
        interactor <- ZIO.access[Has[Interactor]](_.get)
        result <- interactor
          .execute(Input("", ""))(UserCredential(UserAccount.Id("user-id")))
          .parallelErrors
          .either
      } yield {
        assert(result) {
          isLeft(
            isSubtype[List[ValidationError]](anything) &&
            hasSize(equalTo(2))
          )
        }
      }).provideLayer(mockedInteractorLayer)
    }
  )
}
