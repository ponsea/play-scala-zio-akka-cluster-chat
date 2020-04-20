package interfaces.runtime

import akka.actor.typed.Scheduler
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.stream.Materializer
import play.api.BuiltInComponents
import play.api.cluster.sharding.typed.ClusterShardingComponents
import play.api.db.slick.{ SlickApi, SlickComponents }
import play.api.i18n.{ I18nComponents, Langs, MessagesApi }
import play.api.libs.concurrent.{ AkkaComponents, AkkaTypedComponents }
import play.api.mvc.{ ControllerComponents, DefaultControllerComponents }
import zio.{ Has, Layer, ZLayer }

import scala.concurrent.ExecutionContext

object PlayLayerProvider {
  def getControllerComponents(
    builtInComponents: BuiltInComponents
  ): Layer[Nothing, Has[ControllerComponents]] = {
    ZLayer.succeed(
      DefaultControllerComponents(
        builtInComponents.defaultActionBuilder,
        builtInComponents.playBodyParsers,
        builtInComponents.messagesApi,
        builtInComponents.langs,
        builtInComponents.fileMimeTypes,
        builtInComponents.executionContext
      )
    )
  }

  def getI18n(
    i18nComponents: I18nComponents
  ): Layer[Nothing, Has[Langs] with Has[MessagesApi]] = {
    ZLayer.succeedMany(
      Has(i18nComponents.langs) ++
      Has(i18nComponents.messagesApi)
    )
  }

  def getAkka(
    akkaComponents: AkkaComponents
  ): Layer[Nothing, Has[ActorSystem] with Has[CoordinatedShutdown] with Has[ExecutionContext] with Has[Materializer]] = {
    ZLayer.succeedMany(
      Has(akkaComponents.actorSystem) ++
      Has(akkaComponents.coordinatedShutdown) ++
      Has(akkaComponents.executionContext) ++
      Has(akkaComponents.materializer)
    )
  }

  def getAkkaTyped(
    akkaTypedComponents: AkkaTypedComponents
  ): Layer[Nothing, Has[Scheduler]] = {
    ZLayer.succeed(akkaTypedComponents.scheduler)
  }

  def getClusterSharding(
    clusterShardingComponents: ClusterShardingComponents
  ): Layer[Nothing, Has[ClusterSharding]] = {
    ZLayer.succeed(clusterShardingComponents.clusterSharding)
  }

  def getSlick(
    slickComponents: SlickComponents
  ): Layer[Nothing, Has[SlickApi]] = {
    ZLayer.succeed(slickComponents.slickApi)
  }
}
