package domain.usecases

import java.time.Instant

import domain.{ DomainError, DomainEventBus }
import domain.aggregates.comment.{ Comment, CommentEvent, CommentFactory, CommentRepository }
import domain.aggregates.conversation.Conversation
import domain.auth.UserCredential
import domain.utils.CurrentDateTime
import domain.utils.ZioValidation.ZIOValidationOps
import zio.{ IO, UIO, ZLayer }

object SendCommentUseCase {
  case class Input(content: String, conversationKey: String)
  case class Output(createdComment: Comment)

  class Interactor(
    domainEventBus: DomainEventBus,
    commentFactory: CommentFactory,
    commentRepository: CommentRepository,
    currentDateTime: CurrentDateTime[Instant]
  ) {
    def execute(input: Input)(userCredential: UserCredential): IO[DomainError, Output] = {
      for {
        validated <- {
          Comment.Content.validated(input.content) conzip
          Conversation.Key.validated(input.conversationKey)
        }
        (commentContent, conversationKey) = validated
        createdComment <- commentFactory.generate(userCredential.userId, commentContent, conversationKey)
        _              <- commentRepository.save(createdComment)
        now            <- currentDateTime.get()
        _ <- domainEventBus.publish(
          CommentEvent.Sent(createdComment.id,
                            createdComment.content,
                            createdComment.authorId,
                            createdComment.conversationKey,
                            now)
        )
      } yield Output(createdComment)
    }
  }

  object Interactor {
    val layer = ZLayer.fromServices(new Interactor(_, _, _, _))
  }
}
