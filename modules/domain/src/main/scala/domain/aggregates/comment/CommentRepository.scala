package domain.aggregates.comment

import zio.UIO

trait CommentRepository {
  def save(comment: Comment): UIO[Unit]
}
