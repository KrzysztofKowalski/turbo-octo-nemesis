package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.ColumnBase
import org.joda.time.DateTime
import scala.collection.immutable.StringOps._
import scala.Array
import play.api.db.slick._
import scala.Some
import play.api.Play.current
import com.github.tototoshi.slick.JodaSupport._
import play.api.libs.json.Json

case class Comment(id: Option[Long] = None,
                   createdAt: Option[DateTime] = None,
                   updatedAt: Option[DateTime] = None,
                   topicId: Long,
                   content: String)

object Comments extends Table[Comment]("comments") with CRUDSuperPowers[Comment] {

  def content = column[String]("content")

  def topicId = column[Long]("topic_id")

  def * : ColumnBase[Comment] = base ~ topicId ~ content <>(Comment.apply _, Comment.unapply _)

  def generate(topicId: Long, content: String): Comment = Comment(topicId = topicId, content = content, createdAt = Some(DateTime.now()), updatedAt = Some(DateTime.now()))


  def findByTopicId(id: Long): List[Comment] = DB.withSession {
    implicit session: Session =>
      (for {c <- Comments if c.topicId === id} yield c).list
  }
}

