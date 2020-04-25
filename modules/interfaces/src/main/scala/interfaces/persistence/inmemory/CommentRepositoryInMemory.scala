package interfaces.persistence.inmemory

import domain.aggregates.comment.{ Comment, CommentRepository }
import zio.{ Has, IO, Ref, UIO, ULayer, ZIO, ZLayer }

class CommentRepositoryInMemory(commentsRef: Ref[Map[Comment.Id, Comment]]) extends CommentRepository {
  def save(comment: Comment): UIO[Unit] = {
    commentsRef.update(_ + (comment.id -> comment))
  }

  def findById(id: Comment.Id): IO[Unit, Comment] = {
    for {
      comments <- commentsRef.get
      comment  <- ZIO.fromOption(comments.get(id))
    } yield comment
  }
}

object CommentRepositoryInMemory {
  val layer: ULayer[Has[CommentRepository]] = ZLayer.fromEffect {
    Ref.make(Map.empty[Comment.Id, Comment]).map(new CommentRepositoryInMemory(_))
  }
}
