package domain.utils

import java.time.Instant

import zio.{ UIO, ZLayer }
import zio.clock.Clock

trait CurrentDateTime[DateTimeValueType] {
  def get(): UIO[DateTimeValueType]
}

object CurrentDateTime {
  val instantLayer = ZLayer.fromService[
    Clock.Service,
    CurrentDateTime[Instant]
  ](new CurrentDateTimeInstantImpl(_))
}

class CurrentDateTimeInstantImpl(clock: Clock.Service) extends CurrentDateTime[Instant] {
  override def get(): UIO[Instant] = {
    clock.currentDateTime.map(_.toInstant()).orDie
  }
}
