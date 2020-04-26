package domain.services

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit._

import domain.DomainEvent
import domain.aggregates.comment.{ Comment, CommentEvent }
import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount
import eu.timepit.refined.auto._
import mocks.DomainEventBusMock
import zio._
import zio.duration.Duration
import zio.test.Assertion._
import zio.test._
import zio.test.environment.TestClock
import zio.test.mock.Expectation._

object ConversationUpdateSubscriberSpec extends DefaultRunnableSpec {
  val now: Instant = Instant.parse("2000-01-01T00:00:00Z")
  val uuid: UUID   = UUID.fromString("811d4442-c9fc-4b50-9725-843e2f2e6b77")

  override def spec = suite("SendCommentUseCaseSpec")(subscribeByConversationKeySpec)

  def subscribeByConversationKeySpec = suite("subscribeByConversationKey")(
    testM("return a stream which has a buffer (size 16) dropping old") {
      val conversationKey = Conversation.Key("conversation-key")
      val commentSentEvent = CommentEvent.Sent(
        Comment.Id(uuid),
        Comment.Content("valid comment content"),
        UserAccount.Id("author-id"),
        conversationKey,
        now
      )
      val commentSentEvents17               = (1 to 17).map(i => commentSentEvent.copy(occurredAt = now.plusMillis(i)))
      val domainEventQueueM                 = Queue.unbounded[DomainEvent].tap(_.offerAll(commentSentEvents17))
      val mockedDomainEventBusLayer         = DomainEventBusMock.Subscribe returns valueM(_ => domainEventQueueM)
      val conversationUpdateSubscriberLayer = mockedDomainEventBusLayer >>> ConversationUpdateSubscriber.layer
      (for {
        subscriber               <- ZIO.access[Has[ConversationUpdateSubscriber]](_.get)
        conversationUpdateStream <- subscriber.subscribeByConversationKey(conversationKey)
        outputBuffer             <- Queue.unbounded[ConversationUpdate]
        _                        <- conversationUpdateStream.foreach(outputBuffer.offer).fork
        conversationUpdates <- ZIO.foldLeft(1 to 16)(List.empty[ConversationUpdate]) { (acc, _) =>
          outputBuffer.take.map(_ :: acc)
        }
      } yield {
        val actualOccurredTimesOfUpdates   = conversationUpdates.map(_.occuredAt).reverse
        val expectedOccurredTimesOfUpdates = commentSentEvents17.drop(1).map(_.occurredAt).toList
        assert(actualOccurredTimesOfUpdates)(equalTo(expectedOccurredTimesOfUpdates))
      }).provideSomeLayer[ZTestEnv](conversationUpdateSubscriberLayer)
    },
    testM("return a stream which doesn't contain other conversation's updates") {
      val targetConversationKey = Conversation.Key("target-conversation-key")
      val targetCommentSentEvent =
        CommentEvent.Sent(
          Comment.Id(uuid),
          Comment.Content("valid comment content"),
          UserAccount.Id("author-id"),
          targetConversationKey,
          now
        )
      val notTargetConversationKey = Conversation.Key("not-target-conversation-key")
      val notTargetCommentSentEvent =
        CommentEvent.Sent(
          Comment.Id(uuid),
          Comment.Content("valid comment content"),
          UserAccount.Id("author-id"),
          notTargetConversationKey,
          now
        )
      val domainEventQueueM = Queue
        .unbounded[DomainEvent].tap(
          _.offerAll(
            Seq(
              targetCommentSentEvent,
              notTargetCommentSentEvent,
              targetCommentSentEvent,
              notTargetCommentSentEvent
            )
          )
        )
      val mockedDomainEventBusLayer         = DomainEventBusMock.Subscribe returns valueM(_ => domainEventQueueM)
      val conversationUpdateSubscriberLayer = mockedDomainEventBusLayer >>> ConversationUpdateSubscriber.layer
      (for {
        subscriber               <- ZIO.access[Has[ConversationUpdateSubscriber]](_.get)
        conversationUpdateStream <- subscriber.subscribeByConversationKey(targetConversationKey)
        outputBuffer             <- Queue.unbounded[ConversationUpdate]
        _                        <- conversationUpdateStream.foreach(outputBuffer.offer).fork
        conversationUpdates <- ZIO.foldLeft(1 to 2)(List.empty[ConversationUpdate]) { (acc, _) =>
          outputBuffer.take.map(_ :: acc)
        }
      } yield {
        assert(conversationUpdates)(
          forall(hasField("conversationKey", _.conversationKey, equalTo(targetConversationKey)))
        )
      }).provideSomeLayer[ZTestEnv](conversationUpdateSubscriberLayer)
    }
  )
}
