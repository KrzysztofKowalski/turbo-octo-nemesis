package models.remote

import play.api.libs.{ws, json}
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.{ExecutionContext, Future}
import play.api.cache.Cache
import play.api.Play.current
import ExecutionContext.Implicits.global
import play.api.Logger
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

case class Category(id: Long, shortName: String, name: String)

case class Course(id: Long, startDate: LocalDate, name: String, duration: String)

case class Topic(id: Long, name: String, shortName: Option[String], description: Option[String], instructor: Option[String])

/**
 * JSON writers
 */
object CourseraWriters {

  implicit val caw = Json.writes[Category]
  implicit val cow = Json.writes[Course]
  implicit val tw = Json.writes[Topic]
}

/**
 * Object for getting data from Coursera API
 */
object Coursera {

  def getCourseList(names: List[String]) = {
    for (n <- names) yield {
      val c = getCategoryByShortName(n)
      c
    }
  }

  def getCategoryByShortName(s: String): Future[List[Category]] = CourseraWSClient get() map {
    response =>
      (response.json \ "cats").as[List[JsObject]] filter {
        // filter only results with matching short name
        category => (category \ "short_name").as[String] == s
      } map {
        // convert to something statically typed and defined by us not 3rd party site
        c => Category(
          (c \ "id").as[Long],
          (c \ "short_name").as[String],
          (c \ "name").as[String])
      }
  }

  def getTopicsByCategoryId(id: Long): Future[collection.Set[Topic]] = CourseraWSClient get() map {
    response =>
      (response.json \ "topics").as[JsObject].values.filter {
        v => (v \ "cats").asOpt[List[Long]] match {
          case a: Some[List[Long]] => a.get.contains(id)
          case None => false
        }
      }.map {
        // convert that into case class with fields we need
        t => Topic(
          (t \ "id").as[Long],
          (t \ "name").as[String],
          (t \ "short_name").asOpt[String],
          (t \ "short_description").asOpt[String],
          (t \ "instructor").asOpt[String]
        )
      }
  }

  def getTopic(id: Long): Future[Option[Topic]] = CourseraWSClient get() map {
    response =>
      (response.json \ "topics").as[JsObject].values.filter {
        v => (v \ "id").as[Long] == id
      }.map {
        // convert that into case class with fields we need
        t => Topic(
          (t \ "id").as[Long],
          (t \ "name").as[String],
          (t \ "short_name").asOpt[String],
          (t \ "short_description").asOpt[String],
          (t \ "instructor").asOpt[String]
        )
      }.headOption
  }

  def getCoursesByTopicId(id: Long): Future[List[Course]] = CourseraWSClient get() map {
    response =>
      (response.json \ "courses").as[List[JsObject]].filter {
        v => (v \ "topic_id").asOpt[Long] match {
          case Some(a) => a == id
          case None => false
        }
      } filter {
        c => c.keys.contains("start_day") && c.keys.contains("start_month") && c.keys.contains("start_year")
      } map {
        c => Course(
          (c \ "id").as[Long],
          LocalDate.parse(
            (c \ "start_day").asOpt[Int].getOrElse(1).toString + "/" +
              (c \ "start_month").asOpt[Int].getOrElse(1).toString + "/" +
              (c \ "start_year").asOpt[Int].getOrElse(LocalDate.now().year()).toString, DateTimeFormat.forPattern("dd/MM/yyyy")),
          (c \ "name").as[String],
          (c \ "duration_string").as[String]
        )
      }
  }
}

/**
 * Wrapper for Coursera WS with simple caching
 */
object CourseraWSClient {
  val key = "courseraKey"
  val url = "https://www.coursera.org/maestro/api/topic/list2"

  def fetch(): Future[ws.Response] = WS url (url) withRequestTimeout (20000) withFollowRedirects (true) get() map {
    response => Logger.info(s"GET $url returned ${response.status}")
      response.status match {
        case 200 => response
        case _ => throw new RuntimeException("Problem with external service")
      }
  }

  def get(): Future[ws.Response] =
    Cache.getAs[Future[ws.Response]](key) match {
      case Some(value) => {
        Logger.info("Hit!")
        value
      }
      case None => fetch() map {
        x => Logger.info("Miss!, res: " + x.status)
          Cache.set(key, Future(x))
          x
      }
    }

}