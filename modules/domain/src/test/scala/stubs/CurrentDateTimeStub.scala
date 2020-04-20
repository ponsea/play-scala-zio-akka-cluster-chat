package stubs

import java.time.Instant

import domain.utils.CurrentDateTime
import zio.{ Has, UIO, ULayer, ZLayer }

object CurrentDateTimeStub {
  def instantLayer(instant: Instant): ULayer[Has[CurrentDateTime[Instant]]] =
    ZLayer.succeed(new CurrentDateTime[Instant] {
      override def get(): UIO[Instant] = UIO(instant)
    })
}
