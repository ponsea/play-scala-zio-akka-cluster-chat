package stubs

import java.util.UUID

import domain.utils.IdGenerator
import zio.{ Has, UIO, ULayer, ZLayer }

object IdGeneratorStub {
  def uuidLayer(uuid: UUID): ULayer[Has[IdGenerator[UUID]]] =
    ZLayer.succeed(new IdGenerator[UUID] {
      override def generate(): UIO[UUID] = UIO(uuid)
    })
}
