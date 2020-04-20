package domain

import zio.{Queue, UIO}

trait DomainEventBus {
  def publish(domainEvent: DomainEvent): UIO[Unit]

  def subscribe(): UIO[Queue[DomainEvent]]
}
