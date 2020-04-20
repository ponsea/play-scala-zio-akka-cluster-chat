package mocks

import domain.aggregates.comment.{ Comment, CommentRepository }
import zio._
import zio.test.mock.{ Method, Proxy }

object CommentRepositoryMock {

  sealed trait Tag[I, A] extends Method[Has[CommentRepository], I, A] {
    def envBuilder: URLayer[Has[Proxy], Has[CommentRepository]] =
      CommentRepositoryMock.envBuilder
  }

  object Save extends Tag[Comment, Unit]

  private val envBuilder: URLayer[Has[Proxy], Has[CommentRepository]] =
    ZLayer.fromService[Proxy, CommentRepository](
      invoke =>
        new CommentRepository {
          override def save(comment: Comment): UIO[Unit] = invoke(Save, comment)
      }
    )

  val noImplLayer = ZLayer.succeed(
    new CommentRepository {
      override def save(comment: Comment): UIO[Unit] = ???
    }
  )
}
