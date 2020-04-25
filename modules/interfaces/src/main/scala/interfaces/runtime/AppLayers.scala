package interfaces.runtime

import domain.aggregates.comment.CommentFactory
import domain.services.ConversationUpdateSubscriber
import domain.usecases.{ SendCommentUseCase, WatchConversationUpdatesUseCase }
import domain.utils.{ CurrentDateTime, IdGenerator, Logger }
import interfaces.controllers.actions.{ AuthAction, LoggingAction }
import interfaces.controllers.{ ConversationController, ErrorHandler, SendCommentController }
import interfaces.persistence.slick.tables.CommentsTable
import interfaces.persistence.slick.{ CommentRepositoryBySlick, DBIORunner, DatabasePair }
import interfaces.pubsub.DomainEventBusByAkka
import play.api.BuiltInComponents
import play.api.db.slick.{ DbName, SlickApi, SlickComponents }
import slick.jdbc.JdbcProfile
import zio.logging.slf4j.Slf4jLogger
import zio.{ ZEnv, ZLayer }

class AppLayers(
  playComponents: BuiltInComponents with SlickComponents
  // if need..
  // with ClusterShardingComponents
) {
  def playBasics = {
    PlayLayerProvider.getControllerComponents(playComponents) ++
    PlayLayerProvider.getAkka(playComponents) ++
    PlayLayerProvider.getSlick(playComponents)
    // if need..
    // PlayLayerProvider.getAkkaTyped(playComponents)
    // PlayLayerProvider.getClusterSharding(playComponents)
    // PlayLayerProvider.getI18n(playComponents)
  }

  def slickBasicsOfSampleDb = {
    val masterDbName   = DbName("sample-master")
    val readonlyDbName = DbName("sample-readonly")
    val jdbcProfile = playBasics >>> ZLayer.fromService[SlickApi, JdbcProfile] { slickApi =>
      slickApi.dbConfig[JdbcProfile](masterDbName).profile
    }
    val tables = jdbcProfile >>> {
      CommentsTable.layer
    }
    val dbioRunner = playBasics >>> ZLayer.fromService[SlickApi, DBIORunner] { slickApi =>
      new DBIORunner(
        DatabasePair(
          master = slickApi.dbConfig[JdbcProfile](masterDbName).db,
          readonly = slickApi.dbConfig[JdbcProfile](readonlyDbName).db
        )
      )
    }
    tables ++ dbioRunner
  }

  def repositories = {
    slickBasicsOfSampleDb >>> CommentRepositoryBySlick.layer
  }

  // components in `domain.utils.*`
  def utils = {
    val logger                 = Slf4jLogger.make((_, message) => message) >>> Logger.layer
    val currentDateTimeInstant = ZEnv.live >>> CurrentDateTime.instantLayer
    val IdGeneratorUuid        = IdGenerator.uuidLayer
    logger ++ currentDateTimeInstant ++ IdGeneratorUuid
  }

  def factories = utils >>> {
    CommentFactory.layer
  }

  def domainEventBus = (playBasics ++ utils) >>> DomainEventBusByAkka.layer

  // components in `domain.services.*`
  def services = {
    domainEventBus >>> ConversationUpdateSubscriber.layer
  }

  def interactors =
    (repositories ++ utils ++ factories ++ services ++ domainEventBus) >>> {
      SendCommentUseCase.Interactor.layer ++
      WatchConversationUpdatesUseCase.Interactor.layer
    }

  def actionFunctions = utils >>> {
    LoggingAction.layer ++
    AuthAction.layer
  }

  private def domainComponents = utils ++ repositories ++ factories ++ services ++ domainEventBus ++ interactors

  def errorHandler = utils >>> ErrorHandler.layer

  def controllerProviders = (playBasics ++ actionFunctions ++ errorHandler ++ domainComponents) >>> {
    SendCommentController.Provider.layer ++
    ConversationController.Provider.layer
  }
}
