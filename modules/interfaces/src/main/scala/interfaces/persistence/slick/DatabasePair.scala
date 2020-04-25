package interfaces.persistence.slick

import slick.basic.BasicProfile

case class DatabasePair(
  master: BasicProfile#Backend#Database,
  readonly: BasicProfile#Backend#Database
)

object DatabasePair {
  def apply(master: BasicProfile#Backend#Database): DatabasePair =
    apply(master = master, readonly = master)
}
