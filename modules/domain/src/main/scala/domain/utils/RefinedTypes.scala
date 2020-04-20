package domain.utils

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.boolean.And
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.collection.{ NonEmpty, Size }
import eu.timepit.refined.numeric.Less

object RefinedTypes {
  type StringLess255 = Refined[String, Size[Less[W.`255`.T]]]

  type NonEmptyStringLess255 = Refined[String, NonEmpty And Size[Less[W.`255`.T]]]

  type EmailStringLess255 = Refined[String, MatchesRegex[W.`"""[^\\s]@[^\\s]"""`.T] And Size[Less[W.`255`.T]]]
}
