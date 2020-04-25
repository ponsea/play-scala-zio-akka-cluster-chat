package domain.aggregates.comment

import zio.{ IO, UIO }

trait CommentRepository {
  def findById(id: Comment.Id): IO[Unit, Comment]

  def save(comment: Comment): UIO[Unit]
}
