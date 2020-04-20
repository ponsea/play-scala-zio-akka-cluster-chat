package interfaces.json

import domain.aggregates.comment.Comment
import domain.aggregates.conversation.Conversation
import domain.aggregates.useraccount.UserAccount
import play.api.libs.json.{ Json, Writes }
import eu.timepit.refined.auto._

trait CommentJsonWrites {
  implicit private val idWrites              = Writes[Comment.Id](id => Json.toJson(id.underlying))
  implicit private val authorIdWrites        = Writes[UserAccount.Id](authorId => Json.toJson(authorId.underlying))
  implicit private val conversationKeyWrites = Writes[Conversation.Key](key => Json.toJson(key.underlying))
  implicit private val contentWrites         = Writes[Comment.Content](content => Json.toJson(content.underlying))

  implicit val defaultCommentWrites = Json.writes[Comment]
}
