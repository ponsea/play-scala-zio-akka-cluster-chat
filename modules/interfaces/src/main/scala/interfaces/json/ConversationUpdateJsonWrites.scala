package interfaces.json

import domain.aggregates.comment.Comment
import domain.aggregates.conversation.Conversation
import domain.services.ConversationUpdate
import play.api.libs.json.{ JsValue, Json, Writes }
import eu.timepit.refined.auto._

trait ConversationUpdateJsonWrites {
  import ConversationUpdate._

  implicit private val conversationKeyWrites                  = Writes[Conversation.Key](key => Json.toJson(key.underlying))
  implicit private val commentIdWrites                        = Writes[Comment.Id](id => Json.toJson(id.underlying))
  implicit private val conversationUpdateCommentAddedWrites   = Json.writes[CommentAdded]
  implicit private val conversationUpdateCommentUpdatedWrites = Json.writes[CommentUpdated]
  implicit private val conversationUpdateCommentDeletedWrites = Json.writes[CommentDeleted]

  implicit val conversationUpdateWrites = Writes[ConversationUpdate] {
    case update: CommentAdded   => envelope("comment-added", Json.toJson(update))
    case update: CommentUpdated => envelope("comment-updated", Json.toJson(update))
    case update: CommentDeleted => envelope("comment-deleted", Json.toJson(update))
  }

  private def envelope(typeName: String, payload: JsValue) =
    Json.obj(
      "type"    -> typeName,
      "payload" -> payload
    )
}
