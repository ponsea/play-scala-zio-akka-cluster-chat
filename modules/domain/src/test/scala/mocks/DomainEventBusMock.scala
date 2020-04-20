package mocks

import domain.{ DomainEvent, DomainEventBus }
import zio.{ Has, Queue, UIO, URLayer, ZIO, ZLayer }
import zio.test.mock.{ Method, Proxy }

object DomainEventBusMock {

  sealed trait Tag[I, A] extends Method[Has[DomainEventBus], I, A] {
    def envBuilder: URLayer[Has[Proxy], Has[DomainEventBus]] =
      DomainEventBusMock.envBuilder
  }

  object Publish   extends Tag[DomainEvent, Unit]
  object Subscribe extends Tag[Unit, Queue[DomainEvent]]

  private val envBuilder: URLayer[Has[Proxy], Has[DomainEventBus]] =
    ZLayer.fromService[Proxy, DomainEventBus](
      invoke =>
        new DomainEventBus {
          override def publish(domainEvent: DomainEvent): ZIO[Any, Nothing, Unit] = invoke(Publish, domainEvent)

          override def subscribe(): UIO[Queue[DomainEvent]] = invoke(Subscribe)
      }
    )

  val noImplLayer = ZLayer.succeed(
    new DomainEventBus {
      override def publish(domainEvent: DomainEvent): UIO[Unit] = ???

      override def subscribe(): UIO[Queue[DomainEvent]] = ???
    }
  )
}
