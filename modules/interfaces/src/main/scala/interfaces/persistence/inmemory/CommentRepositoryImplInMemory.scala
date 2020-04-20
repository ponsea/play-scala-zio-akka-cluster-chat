package interfaces.persistence.inmemory

import domain.aggregates.comment.{ Comment, CommentRepository }
import zio.{ Ref, UIO, ZLayer }

class CommentRepositoryImplInMemory extends CommentRepository {
  protected val data: UIO[Ref[Map[Comment.Id, Comment]]] = Ref.make(Map.empty)

  def save(comment: Comment): UIO[Unit] = {
    data.flatMap(_.update(_ + (comment.id -> comment)))
  }
}

object CommentRepositoryImplInMemory {
  val layer = ZLayer.succeed[CommentRepository](new CommentRepositoryImplInMemory)
}
