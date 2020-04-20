package interfaces.runtime

import domain.aggregates.comment.CommentFactory
import domain.services.ConversationUpdateSubscriber
import domain.utils.{ CurrentDateTime, IdGenerator, Logger }
import domain.usecases.{ SendCommentUseCase, WatchConversationUpdatesUseCase }
import interfaces.controllers.actions.{ AuthAction, LoggingAction }
import interfaces.controllers.{ ConversationController, ErrorHandler, SendCommentController }
import interfaces.persistence.inmemory.CommentRepositoryImplInMemory
import interfaces.pubsub.DomainEventBusImplByAkka
import play.api.BuiltInComponents
import zio.ZEnv
import zio.logging.slf4j.Slf4jLogger

class AppLayers(
  playComponents: BuiltInComponents
  // if need..
  // with ClusterShardingComponents
  // with SlickComponents
) {
  val controllerComponents = PlayLayerProvider.getControllerComponents(playComponents)
  val akka                 = PlayLayerProvider.getAkka(playComponents)
  // if need..
  // val akkaTyped            = PlayLayerProvider.getAkkaTyped(playComponents)
  // val clusterSharding      = PlayLayerProvider.getClusterSharding(playComponents)
  // val i18n                 = PlayLayerProvider.getI18n(playComponents)
  // val slick                = PlayLayerProvider.getSlick(playComponents)

  val logger = Slf4jLogger.make((_, message) => message) >>> Logger.layer

  val currentDateTimeInstant       = ZEnv.live >>> CurrentDateTime.instantLayer
  val domainEventBus               = (akka ++ logger) >>> DomainEventBusImplByAkka.layer
  val conversationUpdateSubscriber = domainEventBus >>> ConversationUpdateSubscriber.layer

  val loggingAction = logger >>> LoggingAction.layer

  val commentFactory = {
    IdGenerator.uuidLayer ++
    currentDateTimeInstant
  } >>> CommentFactory.layer

  val sendCommentUseCaseInteractor = {
    domainEventBus ++
    commentFactory ++
    CommentRepositoryImplInMemory.layer ++
    currentDateTimeInstant
  } >>> SendCommentUseCase.Interactor.layer

  val watchConversationUpdatesUseCaseInteractor = {
    conversationUpdateSubscriber
  } >>> WatchConversationUpdatesUseCase.Interactor.layer

  val errorHandler = logger >>> ErrorHandler.layer

  val sendCommentController = {
    controllerComponents ++
    sendCommentUseCaseInteractor ++
    loggingAction ++
    AuthAction.layer ++
    errorHandler
  } >>> SendCommentController.Provider.layer

  val conversationControllerProvider = {
    controllerComponents ++
    watchConversationUpdatesUseCaseInteractor ++
    logger ++
    errorHandler
  } >>> ConversationController.Provider.layer
}
