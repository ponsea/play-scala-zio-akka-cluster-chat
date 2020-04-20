package domain

import java.time.Instant

trait DomainEvent {
  def version: Int = 1
  def occurredAt: Instant
}
