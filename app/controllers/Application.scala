package controllers

import play.api.mvc._
import models.remote.Coursera
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.cache.Cached
import play.api.Play.current
import play.api.mvc._
import play.api.mvc.Security
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import models.Comments

object Application extends Controller {

  /**
   * really simple comment form
   */
  val commentForm = Form(
    tuple(
      "topicId" -> number,
      "content" -> nonEmptyText(10, 255)
    )
  )

  import models.remote.CourseraWriters._

  def index = Action {
    Ok(views.html.index("Welcome to test app."))
  }

  def category(name: String) = Cached(request => request.uri, 1440) {
    Action.async {
      Coursera.getCategoryByShortName(name) map {
        x => Ok(Json.toJson(x.head))
      }
    }
  }

  def topic(id: Long) = Cached(request => request.uri, 1440) {
    Action.async {
      Coursera.getTopic(id) map {
        x => Ok(Json.toJson(x))
      }
    }
  }

  def topics(id: Long) = Cached(request => request.uri, 1440) {
    Action.async {
      Coursera.getTopicsByCategoryId(id) map {
        x => Ok(Json.toJson(x))
      }
    }
  }

  def course(id: Long) = Cached(request => request.uri, 1440) {
    Action.async {
      Coursera.getCoursesByTopicId(id) map {
        x => Ok(Json.toJson(x))
      }
    }
  }

  // commments and dragons live here

  def comments(id: Long) = Action {
    import models.JsonSerializers._
    Ok(Json.toJson(
      Comments.findByTopicId(id)
    ))
  }

  def createComment = Action {
    implicit request =>
      commentForm.bindFromRequest() fold(
        br => BadRequest(JsObject(Seq("status" -> JsBoolean(false), "msg" -> JsString("Bad request - errors in form"), "errors" -> br.errorsAsJson))),
        cc => {
          val cid = try {
            Comments.insert(Comments.generate(cc._1, cc._2))
          } catch {
            case _: java.sql.SQLException => -1
          }
          cid match {
            case -1 => Ok(Json.toJson(false))
            case _ => Ok(Json.toJson(true))
          }
        }
        )
  }

}