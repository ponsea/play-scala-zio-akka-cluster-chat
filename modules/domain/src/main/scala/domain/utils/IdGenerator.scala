package domain.utils

import java.util.UUID

import zio.{ UIO, ZLayer }

trait IdGenerator[IdValueType] {
  def generate(): UIO[IdValueType]
}

object IdGenerator {
  val uuidLayer = ZLayer.succeed[IdGenerator[UUID]](new IdGeneratorUUIDImpl)
}

class IdGeneratorUUIDImpl extends IdGenerator[UUID] {
  override def generate(): UIO[UUID] = UIO(UUID.randomUUID())
}
