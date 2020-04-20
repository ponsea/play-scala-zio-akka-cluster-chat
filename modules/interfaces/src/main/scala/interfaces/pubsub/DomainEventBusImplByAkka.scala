package interfaces.pubsub

import akka.actor.ActorSystem
import domain.utils.Logger
import domain.{ DomainEvent, DomainEventBus }
import zio.akka.cluster.pubsub.PubSub
import zio.{ Has, Queue, UIO, ZLayer }

class DomainEventBusImplByAkka(actorSystem: ActorSystem, logger: Logger) extends DomainEventBus {
  val topicKey = "domain-events"

  private val createPubSub = PubSub.createPubSub[DomainEvent].provide(Has(actorSystem))

  def publish(domainEvent: DomainEvent): UIO[Unit] = {
    createPubSub
      .flatMap(_.publish(topicKey, domainEvent))
      .tapBoth(
        error => logger.throwable(s"Failed to publish the domain event: $domainEvent", error),
        _ => logger.info(s"Domain event published. $domainEvent")
      )
      .orDie
  }

  def subscribe(): UIO[Queue[DomainEvent]] = {
    createPubSub
      .flatMap(_.listen(topicKey))
      .orDie
  }
}

object DomainEventBusImplByAkka {
  val layer = ZLayer.fromServices[ActorSystem, Logger, DomainEventBus](
    new DomainEventBusImplByAkka(_, _)
  )
}
